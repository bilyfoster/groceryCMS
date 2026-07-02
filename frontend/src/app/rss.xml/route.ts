const BACKEND = process.env.BACKEND_URL || "http://localhost:8081";
const TENANT = process.env.NEXT_PUBLIC_TENANT_SLUG || "demo";

export const revalidate = 300;

/**
 * Serves the blog RSS feed at a clean, discoverable URL. Proxies the backend
 * feed server-side with the tenant header so it resolves correctly even when a
 * reader or browser requests it directly.
 */
export async function GET() {
  try {
    const res = await fetch(`${BACKEND}/api/blog/rss`, {
      headers: { "X-Tenant-Slug": TENANT },
      next: { revalidate: 300 },
    });
    const xml = await res.text();
    return new Response(xml, {
      status: res.ok ? 200 : 502,
      headers: { "Content-Type": "application/rss+xml; charset=utf-8" },
    });
  } catch {
    return new Response(
      '<?xml version="1.0" encoding="UTF-8"?><rss version="2.0"><channel><title>Blog</title></channel></rss>',
      { status: 200, headers: { "Content-Type": "application/rss+xml; charset=utf-8" } },
    );
  }
}
