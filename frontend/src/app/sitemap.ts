import type { MetadataRoute } from "next";
import { fetchBlogPosts, fetchNavPages, fetchTenantSettings } from "@/lib/api";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const tenant = await fetchTenantSettings().catch(() => null);
  const domain =
    (tenant?.settings?.domain as string | undefined) ||
    process.env.NEXT_PUBLIC_SITE_URL?.replace(/^https?:\/\//, "") ||
    "localhost:3000";
  const protocol = domain.includes("localhost") ? "http" : "https";
  const base = `${protocol}://${domain}`;

  const [pages, posts] = await Promise.all([
    fetchNavPages().catch(() => []),
    fetchBlogPosts(0, 100).catch(() => ({ items: [] })),
  ]);

  return [
    { url: base, lastModified: new Date() },
    ...pages.map((p) => ({
      url: `${base}${p.slug === "home" ? "" : `/${p.slug}`}`,
      lastModified: new Date(),
    })),
    ...posts.items.map((p) => ({
      url: `${base}/blog/${p.slug}`,
      lastModified: p.publishedAt ? new Date(p.publishedAt) : new Date(),
    })),
  ];
}
