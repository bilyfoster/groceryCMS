import type { Metadata } from "next";
import type { BlogPostDetail } from "@/types/blog";
import type { PageDetail } from "@/types/page";
import type { TenantSettingsDto } from "@/types/tenant";

function siteUrl(tenant: TenantSettingsDto | null): string {
  const domain =
    (tenant?.settings?.domain as string | undefined) ||
    process.env.APP_BASE_URL?.replace(/^https?:\/\//, "") ||
    process.env.NEXT_PUBLIC_SITE_URL?.replace(/^https?:\/\//, "") ||
    "localhost:3000";
  const protocol = domain.includes("localhost") ? "http" : "https";
  return `${protocol}://${domain}`;
}

export function generatePageMetadata(
  page: PageDetail,
  tenant: TenantSettingsDto | null,
): Metadata {
  const base = siteUrl(tenant);
  const title = page.metaTitle ?? page.title;
  const description = page.metaDescription ?? undefined;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      images: page.ogImageUrl ? [{ url: page.ogImageUrl }] : [],
      type: "website",
    },
    twitter: { card: "summary_large_image" },
    alternates: {
      canonical: `${base}/${page.slug === "home" ? "" : page.slug}`.replace(
        /\/$/,
        "",
      ) || base,
    },
  };
}

export function generatePostMetadata(
  post: BlogPostDetail,
  tenant: TenantSettingsDto | null,
): Metadata {
  const base = siteUrl(tenant);
  const title = post.metaTitle ?? post.title;
  const description = post.metaDescription ?? post.excerpt ?? undefined;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      images: post.featuredImage ? [{ url: post.featuredImage }] : [],
      type: "article",
      publishedTime: post.publishedAt ?? undefined,
    },
    twitter: { card: "summary_large_image" },
    alternates: {
      canonical: `${base}/blog/${post.slug}`,
    },
  };
}

export function articleJsonLd(
  post: BlogPostDetail,
  tenant: TenantSettingsDto | null,
) {
  const base = siteUrl(tenant);
  return {
    "@context": "https://schema.org",
    "@type": "Article",
    headline: post.title,
    datePublished: post.publishedAt,
    dateModified: post.publishedAt,
    author: {
      "@type": "Organization",
      name: tenant?.name ?? "BrochureCMS",
    },
    mainEntityOfPage: `${base}/blog/${post.slug}`,
  };
}
