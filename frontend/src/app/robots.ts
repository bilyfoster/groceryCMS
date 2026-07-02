import type { MetadataRoute } from "next";
import { fetchTenantSettings } from "@/lib/api";

export default async function robots(): Promise<MetadataRoute.Robots> {
  const tenant = await fetchTenantSettings().catch(() => null);
  const domain =
    (tenant?.settings?.domain as string | undefined) ||
    process.env.NEXT_PUBLIC_SITE_URL?.replace(/^https?:\/\//, "") ||
    "localhost:3000";
  const protocol = domain.includes("localhost") ? "http" : "https";

  return {
    rules: {
      userAgent: "*",
      allow: "/",
      disallow: ["/admin/", "/api/"],
    },
    sitemap: `${protocol}://${domain}/sitemap.xml`,
  };
}
