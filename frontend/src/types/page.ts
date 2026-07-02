export type PageType =
  | "home"
  | "blog"
  | "faq"
  | "contact"
  | "staff"
  | "gallery"
  | "service"
  | "custom"
  | "404";

export interface PageSummary {
  id: string;
  slug: string;
  title: string;
  pageType: PageType;
  layout: string;
  navOrder: number | null;
  published: boolean;
  frontPage?: boolean;
  postsPage?: boolean;
}

export interface ContentBlock {
  id: string;
  blockType: string;
  sortOrder: number;
  content: Record<string, unknown>;
  published: boolean;
}

export interface PageDetail {
  id: string;
  slug: string;
  title: string;
  pageType: PageType;
  layout: string;
  navOrder: number | null;
  published: boolean;
  frontPage?: boolean;
  postsPage?: boolean;
  passwordProtected?: boolean;
  metaTitle: string | null;
  metaDescription: string | null;
  ogImageUrl: string | null;
  config: Record<string, unknown>;
  blocks: ContentBlock[];
}

export interface StaffMember {
  id: string;
  pageId?: string;
  name: string;
  title: string | null;
  bio: string | null;
  photoUrl: string | null;
  email: string | null;
  sortOrder: number;
  published?: boolean;
  isTherapist?: boolean;
  therapistId?: string | null;
  socialLinks: Record<string, string>;
}

export interface FaqItem {
  id: string;
  question: string;
  answer: string;
  sortOrder: number;
}

export interface GalleryImage {
  id: string;
  url: string;
  altText: string | null;
  caption: string | null;
  sortOrder: number;
}
