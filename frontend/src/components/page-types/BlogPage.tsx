import Link from "next/link";
import { fetchBlogPosts } from "@/lib/api";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export async function BlogPage({ page }: PageTypeProps) {
  const layout = page.layout || "grid";
  const posts = await fetchBlogPosts(0, 24);

  return (
    <article className="mx-auto max-w-6xl px-4 py-8">
      <BlockRenderer blocks={page.blocks} />
      <h1 className="mb-6 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
        {page.title}
      </h1>
      <ul
        className={
          layout === "list"
            ? "flex flex-col gap-4"
            : layout === "magazine"
              ? "grid gap-6 md:grid-cols-2"
              : "grid gap-6 sm:grid-cols-2 lg:grid-cols-3"
        }
      >
        {posts.items.map((post) => (
          <li key={post.id}>
            <Card>
              <CardHeader>
                <CardTitle>
                  <Link
                    href={`/blog/${post.slug}`}
                    className="inline-flex min-h-[44px] items-center hover:text-[var(--color-primary)]"
                  >
                    {post.title}
                  </Link>
                </CardTitle>
              </CardHeader>
              {post.excerpt && (
                <CardContent>
                  <p className="text-sm text-slate-600">{post.excerpt}</p>
                </CardContent>
              )}
            </Card>
          </li>
        ))}
      </ul>
      {posts.items.length === 0 && (
        <p className="text-slate-600">No blog posts published yet.</p>
      )}
    </article>
  );
}
