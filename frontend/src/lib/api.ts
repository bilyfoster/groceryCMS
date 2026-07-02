import type { ApiResponse, PagedResponse, UserDto } from "@/types/api";
import type { BlogPostDetail, BlogSummary } from "@/types/blog";
import type {
  FaqItem,
  GalleryImage,
  PageDetail,
  PageSummary,
  StaffMember,
} from "@/types/page";
import type { TenantSettingsDto } from "@/types/tenant";
import type { ContentBlock } from "@/types/page";
import type { MenuItemDto } from "@/types/menu";
import type { TaxonomyTerm, TaxonomyTermRequest, TaxonomyType } from "@/types/taxonomy";
import type {
  ProductDetail,
  ProductFilters,
  ProductRequest,
  ProductSummary,
} from "@/types/product";
import type { IntakeRequest, MatchResponse } from "@/types/match";
import type {
  IntakeMatchRequest,
  IntakeQuestionnaire,
} from "@/types/intake";

const TENANT_SLUG = process.env.NEXT_PUBLIC_TENANT_SLUG || "demo";

const SERVER_BACKEND =
  process.env.BACKEND_URL || "http://localhost:8081";

/** Browser: same-origin `/api` proxy. Server: direct backend URL. */
export function resolveApiBase(): string {
  if (typeof window !== "undefined") {
    return "/api";
  }
  const configured = process.env.NEXT_PUBLIC_API_URL;
  if (configured?.startsWith("http")) {
    return configured.replace(/\/$/, "");
  }
  return `${SERVER_BACKEND.replace(/\/$/, "")}/api`;
}

export const API_BASE = resolveApiBase();

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public errors?: Record<string, string[]> | null,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type FetchOptions = RequestInit & {
  params?: Record<string, string | number | boolean | undefined>;
};

export function buildUrl(path: string, params?: FetchOptions["params"]): string {
  const base = resolveApiBase().replace(/\/$/, "");
  const normalized = path.startsWith("/") ? path : `/${path}`;
  const origin =
    typeof window !== "undefined"
      ? window.location.origin
      : process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";
  const url = base.startsWith("http")
    ? new URL(`${base}${normalized}`)
    : new URL(`${base}${normalized}`, origin);
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        url.searchParams.set(key, String(value));
      }
    });
  }
  return url.toString();
}

async function serverCookieHeader(): Promise<string | undefined> {
  if (typeof window !== "undefined") {
    return undefined;
  }
  const { cookies } = await import("next/headers");
  const jar = cookies();
  const parts: string[] = [];
  const auth = jar.get("auth_token");
  const pageUnlock = jar.get("page_unlock");
  if (auth) parts.push(`auth_token=${auth.value}`);
  if (pageUnlock) parts.push(`page_unlock=${pageUnlock.value}`);
  return parts.length > 0 ? parts.join("; ") : undefined;
}

export async function apiFetch<T>(
  path: string,
  options: FetchOptions = {},
): Promise<T> {
  const { params, headers, ...init } = options;
  const url = buildUrl(path, params);
  const cookieHeader = await serverCookieHeader();

  const response = await fetch(url, {
    ...init,
    credentials: "include",
    cache: "no-store",
    headers: {
      Accept: "application/json",
      "X-Requested-With": "XMLHttpRequest",
      "X-Tenant-Slug": TENANT_SLUG,
      ...(cookieHeader ? { Cookie: cookieHeader } : {}),
      ...(init.body instanceof FormData
        ? {}
        : { "Content-Type": "application/json" }),
      ...headers,
    },
  });

  let envelope: ApiResponse<T>;
  try {
    envelope = (await response.json()) as ApiResponse<T>;
  } catch {
    throw new ApiError("Invalid API response", response.status);
  }

  if (!response.ok || !envelope.success) {
    throw new ApiError(
      envelope.message || response.statusText || "Request failed",
      response.status,
      envelope.errors,
    );
  }

  return envelope.data;
}

export async function fetchTenantSettings(): Promise<TenantSettingsDto> {
  return apiFetch<TenantSettingsDto>("/tenant/settings");
}

export async function fetchNavPages(): Promise<PageSummary[]> {
  return apiFetch<PageSummary[]>("/pages");
}

export async function fetchAdminPages(): Promise<PageSummary[]> {
  return apiFetch<PageSummary[]>("/admin/pages");
}

export async function fetchAdminPage(id: string): Promise<PageDetail> {
  return apiFetch<PageDetail>(`/admin/pages/${id}`);
}

export async function fetchPage(slug: string): Promise<PageDetail | null> {
  try {
    return await apiFetch<PageDetail>(`/pages/${slug}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

export async function fetchFrontPage(): Promise<PageDetail | null> {
  try {
    return await apiFetch<PageDetail>("/pages/front");
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

export async function fetchNotFoundPage(): Promise<PageDetail | null> {
  try {
    return await apiFetch<PageDetail>("/pages/not-found");
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

export async function fetchMenu(location: string): Promise<MenuItemDto[]> {
  return apiFetch<MenuItemDto[]>(`/menus/${location}`);
}

export interface ReadingSettingsDto {
  frontPageId: string | null;
  postsPageId: string | null;
}

export async function fetchReadingSettings(): Promise<ReadingSettingsDto> {
  return apiFetch<ReadingSettingsDto>("/admin/pages/reading");
}

export async function updateReadingSettings(data: ReadingSettingsDto): Promise<ReadingSettingsDto> {
  return apiFetch<ReadingSettingsDto>("/admin/pages/reading", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export interface SearchHit {
  type: string;
  id: string;
  title: string;
  href: string;
  excerpt: string | null;
}

export interface SearchResults {
  pages: SearchHit[];
  posts: SearchHit[];
}

export async function searchSite(q: string): Promise<SearchResults> {
  return apiFetch<SearchResults>("/search", { params: { q } });
}

export async function fetchStaff(pageId: string): Promise<StaffMember[]> {
  return apiFetch<StaffMember[]>("/staff", { params: { pageId } });
}

export interface CreateStaffRequest {
  pageId: string;
  name: string;
  title?: string;
  bio?: string;
  photoUrl?: string;
  email?: string;
  sortOrder?: number;
  published?: boolean;
  socialLinks?: Record<string, string>;
}

export interface UpdateStaffRequest {
  name?: string;
  title?: string;
  bio?: string;
  photoUrl?: string;
  email?: string;
  sortOrder?: number;
  published?: boolean;
  socialLinks?: Record<string, string>;
}

export async function fetchAdminStaff(pageId: string): Promise<StaffMember[]> {
  return apiFetch<StaffMember[]>("/admin/staff", { params: { pageId } });
}

export async function createStaffMember(data: CreateStaffRequest): Promise<StaffMember> {
  return apiFetch<StaffMember>("/admin/staff", { method: "POST", body: JSON.stringify(data) });
}

export async function updateStaffMember(id: string, data: UpdateStaffRequest): Promise<StaffMember> {
  return apiFetch<StaffMember>(`/admin/staff/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

export async function deleteStaffMember(id: string): Promise<void> {
  await apiFetch<void>(`/admin/staff/${id}`, { method: "DELETE" });
}

export async function fetchFaq(pageId: string): Promise<FaqItem[]> {
  return apiFetch<FaqItem[]>("/faq", { params: { pageId } });
}

export async function fetchGallery(pageId: string): Promise<GalleryImage[]> {
  return apiFetch<GalleryImage[]>("/gallery", { params: { pageId } });
}

export async function fetchBlogPosts(
  page = 0,
  size = 12,
): Promise<PagedResponse<BlogSummary>> {
  return apiFetch<PagedResponse<BlogSummary>>("/blog", {
    params: { page, size },
  });
}

export async function fetchBlogPost(slug: string): Promise<BlogPostDetail | null> {
  try {
    return await apiFetch<BlogPostDetail>(`/blog/${slug}`);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

export async function incrementBlogView(slug: string): Promise<number> {
  return apiFetch<number>(`/blog/${slug}/view`, { method: "POST" });
}

export async function likeBlogPost(slug: string): Promise<number> {
  return apiFetch<number>(`/blog/${slug}/like`, { method: "POST" });
}

export async function unlikeBlogPost(slug: string): Promise<number> {
  return apiFetch<number>(`/blog/${slug}/like`, { method: "DELETE" });
}

export async function submitContact(data: {
  name: string;
  email: string;
  phone?: string;
  subject?: string;
  message: string;
}) {
  return apiFetch<unknown>("/contact", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function fetchCurrentUser(): Promise<UserDto | null> {
  try {
    return await apiFetch<UserDto>("/auth/me");
  } catch {
    return null;
  }
}

export interface MediaFileDto {
  id: string;
  filename: string;
  storagePath: string;
  mimeType: string;
  sizeBytes: number;
  altText: string | null;
}

export interface ContactSubmissionDto {
  id: string;
  name: string;
  email: string;
  phone: string | null;
  subject: string | null;
  message: string;
  readAt: string | null;
  createdAt: string;
}

export async function fetchAdminContacts(): Promise<ContactSubmissionDto[]> {
  return apiFetch<ContactSubmissionDto[]>("/admin/contacts");
}

export async function markContactRead(id: string): Promise<ContactSubmissionDto> {
  return apiFetch<ContactSubmissionDto>(`/admin/contacts/${id}/read`, {
    method: "PATCH",
  });
}

export async function fetchAdminMedia(): Promise<MediaFileDto[]> {
  return apiFetch<MediaFileDto[]>("/admin/media");
}

export async function uploadMedia(
  file: File,
  altText?: string,
  onProgress?: (pct: number) => void,
): Promise<MediaFileDto> {
  const form = new FormData();
  form.append("file", file);
  if (altText) form.append("altText", altText);

  const xhr = new Promise<MediaFileDto>((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("POST", buildUrl("/admin/media"));
    request.withCredentials = true;
    request.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    request.setRequestHeader("X-Tenant-Slug", TENANT_SLUG);

    request.upload.onprogress = (event) => {
      if (event.lengthComputable && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100));
      }
    };

    request.onload = () => {
      try {
        const envelope = JSON.parse(request.responseText) as ApiResponse<MediaFileDto>;
        if (request.status >= 200 && request.status < 300 && envelope.success) {
          resolve(envelope.data);
        } else {
          reject(
            new ApiError(
              envelope.message || "Upload failed",
              request.status,
              envelope.errors,
            ),
          );
        }
      } catch {
        reject(new ApiError("Upload failed", request.status));
      }
    };

    request.onerror = () => reject(new ApiError("Upload failed", 0));
    request.send(form);
  });

  return xhr;
}

export async function deleteMedia(id: string): Promise<void> {
  await apiFetch<void>(`/admin/media/${id}`, { method: "DELETE" });
}

export async function createPage(data: {
  slug: string;
  title: string;
  pageType: string;
  layout?: string;
  navOrder?: number;
  metaTitle?: string;
  metaDescription?: string;
  config?: Record<string, unknown>;
}): Promise<PageDetail> {
  return apiFetch<PageDetail>("/admin/pages", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updatePage(
  id: string,
  data: Record<string, unknown>,
): Promise<PageDetail> {
  return apiFetch<PageDetail>(`/admin/pages/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function publishPage(
  id: string,
  published: boolean,
): Promise<PageDetail> {
  return apiFetch<PageDetail>(`/admin/pages/${id}/publish`, {
    method: "PATCH",
    body: JSON.stringify({ published }),
  });
}

export async function deletePage(id: string): Promise<void> {
  await apiFetch<void>(`/admin/pages/${id}`, { method: "DELETE" });
}

export async function reorderBlocks(
  pageId: string,
  orderedIds: string[],
): Promise<PageDetail["blocks"]> {
  return apiFetch<PageDetail["blocks"]>(`/admin/pages/${pageId}/blocks/order`, {
    method: "PATCH",
    body: JSON.stringify({ orderedIds }),
  });
}

export async function createContentBlock(
  pageId: string,
  data: {
    blockType: string;
    content?: Record<string, unknown>;
    sortOrder?: number;
    published?: boolean;
  },
): Promise<ContentBlock> {
  return apiFetch<ContentBlock>(`/admin/pages/${pageId}/blocks`, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateContentBlock(
  pageId: string,
  blockId: string,
  data: {
    blockType?: string;
    content?: Record<string, unknown>;
    sortOrder?: number;
    published?: boolean;
  },
): Promise<ContentBlock> {
  return apiFetch<ContentBlock>(`/admin/pages/${pageId}/blocks/${blockId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteContentBlock(
  pageId: string,
  blockId: string,
): Promise<void> {
  await apiFetch<void>(`/admin/pages/${pageId}/blocks/${blockId}`, {
    method: "DELETE",
  });
}

export async function fetchAdminSettings(): Promise<TenantSettingsDto> {
  return apiFetch<TenantSettingsDto>("/admin/settings");
}

export async function updateAdminSettings(data: {
  name: string;
  settings: Record<string, unknown>;
}): Promise<TenantSettingsDto> {
  return apiFetch<TenantSettingsDto>("/admin/settings", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function createBlogPost(data: {
  pageId: string;
  slug: string;
  title: string;
  excerpt?: string;
  body: string;
  featuredImage?: string;
  tags?: string[];
  categoryIds?: string[];
  sticky?: boolean;
  allowComments?: boolean;
  metaTitle?: string;
  metaDescription?: string;
}): Promise<BlogPostDetail> {
  return apiFetch<BlogPostDetail>("/admin/blog", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateBlogPost(
  id: string,
  data: Record<string, unknown>,
): Promise<BlogPostDetail> {
  return apiFetch<BlogPostDetail>(`/admin/blog/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function publishBlogPost(
  id: string,
  published: boolean,
): Promise<BlogPostDetail> {
  return apiFetch<BlogPostDetail>(`/admin/blog/${id}/publish`, {
    method: "PATCH",
    body: JSON.stringify({ published }),
  });
}

export async function deleteBlogPost(id: string): Promise<void> {
  await apiFetch<void>(`/admin/blog/${id}`, { method: "DELETE" });
}

export async function unlockPage(slug: string, password: string): Promise<void> {
  await apiFetch<void>(`/pages/${slug}/unlock`, {
    method: "POST",
    body: JSON.stringify({ password }),
  });
}

export interface CategoryTree {
  id: string;
  parentId: string | null;
  name: string;
  slug: string;
  description: string | null;
  children: CategoryTree[];
}

export async function fetchCategories(): Promise<CategoryTree[]> {
  return apiFetch<CategoryTree[]>("/categories");
}

export async function createCategory(data: {
  name: string;
  slug: string;
  description?: string;
  parentId?: string | null;
}) {
  return apiFetch<{ id: string; name: string; slug: string }>("/admin/categories", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateCategory(
  id: string,
  data: { name?: string; slug?: string; description?: string; parentId?: string | null },
) {
  return apiFetch<{ id: string; name: string; slug: string }>(`/admin/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteCategory(id: string): Promise<void> {
  await apiFetch<void>(`/admin/categories/${id}`, { method: "DELETE" });
}

export async function fetchTaxonomies(type: TaxonomyType): Promise<TaxonomyTerm[]> {
  return apiFetch<TaxonomyTerm[]>("/taxonomies", { params: { type } });
}

export async function fetchAdminTaxonomies(type: TaxonomyType): Promise<TaxonomyTerm[]> {
  return apiFetch<TaxonomyTerm[]>("/admin/taxonomies", { params: { type } });
}

export async function createTaxonomyTerm(data: TaxonomyTermRequest): Promise<TaxonomyTerm> {
  return apiFetch<TaxonomyTerm>("/admin/taxonomies", { method: "POST", body: JSON.stringify(data) });
}

export async function updateTaxonomyTerm(
  id: string,
  data: TaxonomyTermRequest,
): Promise<TaxonomyTerm> {
  return apiFetch<TaxonomyTerm>(`/admin/taxonomies/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteTaxonomyTerm(id: string): Promise<void> {
  await apiFetch<void>(`/admin/taxonomies/${id}`, { method: "DELETE" });
}

export async function fetchProducts(
  filters: ProductFilters = {},
  page = 0,
  size = 24,
): Promise<PagedResponse<ProductSummary>> {
  const params: Record<string, string | number | boolean | undefined> = { page, size };
  if (filters.allergyType) params.allergyType = filters.allergyType;
  if (filters.dietType) params.dietType = filters.dietType;
  if (filters.category) params.category = filters.category;
  if (filters.storeSection) params.storeSection = filters.storeSection;
  if (filters.stockStatus) params.stockStatus = filters.stockStatus;
  if (filters.q) params.q = filters.q;
  return apiFetch<PagedResponse<ProductSummary>>("/products", { params });
}

export async function fetchProduct(slug: string): Promise<ProductDetail> {
  return apiFetch<ProductDetail>(`/products/${slug}`);
}

export async function fetchAdminProducts(): Promise<ProductDetail[]> {
  return apiFetch<ProductDetail[]>("/admin/products");
}

export async function fetchAdminProduct(id: string): Promise<ProductDetail> {
  return apiFetch<ProductDetail>(`/admin/products/${id}`);
}

export async function createProduct(data: ProductRequest): Promise<ProductDetail> {
  return apiFetch<ProductDetail>("/admin/products", { method: "POST", body: JSON.stringify(data) });
}

export async function updateProduct(id: string, data: ProductRequest): Promise<ProductDetail> {
  return apiFetch<ProductDetail>(`/admin/products/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteProduct(id: string): Promise<void> {
  await apiFetch<void>(`/admin/products/${id}`, { method: "DELETE" });
}

export async function publishProduct(id: string, published: boolean): Promise<ProductDetail> {
  return apiFetch<ProductDetail>(`/admin/products/${id}/publish`, {
    method: "PATCH",
    params: { published },
  });
}

export async function submitMatch(
  data: IntakeRequest,
  captchaToken?: string,
): Promise<MatchResponse> {
  return apiFetch<MatchResponse>("/match", {
    method: "POST",
    body: JSON.stringify(data),
    params: captchaToken ? { captchaToken } : undefined,
  });
}

export async function fetchIntakeQuestionnaire(): Promise<IntakeQuestionnaire> {
  return apiFetch<IntakeQuestionnaire>("/intake/questionnaire");
}

export async function submitIntakeMatch(data: IntakeMatchRequest): Promise<MatchResponse> {
  return apiFetch<MatchResponse>("/intake/match", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function recordEvent(
  eventType: string,
  metadata?: Record<string, unknown>,
): Promise<void> {
  await apiFetch<unknown>("/events", {
    method: "POST",
    body: JSON.stringify({ eventType, metadata }),
  });
}

export interface AdminMenuItem {
  id: string;
  parentId: string | null;
  label: string;
  url: string | null;
  pageId: string | null;
  target: string;
  sortOrder: number;
}

export interface AdminMenu {
  id: string | null;
  location: string;
  name: string;
  items: AdminMenuItem[];
}

export async function fetchAdminMenu(location: string): Promise<AdminMenu> {
  return apiFetch<AdminMenu>(`/admin/menus/${location}`);
}

export async function saveAdminMenu(
  location: string,
  data: {
    name?: string;
    items: Array<{
      parentId?: string | null;
      label: string;
      url?: string | null;
      pageId?: string | null;
      target?: string;
      sortOrder?: number;
    }>;
  },
): Promise<AdminMenu> {
  return apiFetch<AdminMenu>(`/admin/menus/${location}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export interface RevisionSummary {
  id: string;
  authorId: string | null;
  createdAt: string;
}

export async function fetchRevisions(
  entityType: "page" | "blog_post",
  entityId: string,
): Promise<RevisionSummary[]> {
  return apiFetch<RevisionSummary[]>("/admin/revisions", {
    params: { entityType, entityId },
  });
}

export async function restoreRevision(id: string): Promise<void> {
  await apiFetch<void>(`/admin/revisions/${id}/restore`, { method: "POST" });
}

export interface BlockPattern {
  id: string;
  name: string;
  category: string;
  thumbnailUrl: string | null;
  blocks: Array<{ blockType: string; content: Record<string, unknown> }>;
  system: boolean;
}

export async function fetchBlockPatterns(): Promise<BlockPattern[]> {
  return apiFetch<BlockPattern[]>("/admin/block-patterns");
}

export async function createBlockPattern(data: {
  name: string;
  category: string;
  blocks: Array<{ blockType: string; content: Record<string, unknown> }>;
}): Promise<BlockPattern> {
  return apiFetch<BlockPattern>("/admin/block-patterns", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function deleteBlockPattern(id: string): Promise<void> {
  await apiFetch<void>(`/admin/block-patterns/${id}`, { method: "DELETE" });
}

export async function fetchAdminBlogPosts(): Promise<
  Array<BlogSummary & { published: boolean }>
> {
  return apiFetch("/admin/blog");
}

export async function fetchAdminBlogPost(id: string): Promise<BlogPostDetail> {
  return apiFetch<BlogPostDetail>(`/admin/blog/${id}`);
}

export async function fetchBlogByCategory(
  slug: string,
  page = 0,
  size = 12,
): Promise<PagedResponse<BlogSummary>> {
  return apiFetch<PagedResponse<BlogSummary>>(`/blog/category/${slug}`, {
    params: { page, size },
  });
}

export function mediaUrl(storagePath: string): string {
  const base =
    typeof window !== "undefined"
      ? ""
      : SERVER_BACKEND.replace(/\/$/, "");
  return `${base}/media/${storagePath}`;
}
