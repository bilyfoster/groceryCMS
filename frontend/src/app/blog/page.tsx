import Link from "next/link";
import Image from "next/image";
import type { Metadata } from "next";
import { Eye, Heart, Rss } from "lucide-react";
import { fetchBlogPosts } from "@/lib/api";

export const metadata: Metadata = {
  title: "Blog",
  description: "Latest news and updates",
  alternates: {
    types: { "application/rss+xml": "/rss.xml" },
  },
};

export default async function BlogIndexPage() {
  const posts = await fetchBlogPosts(0, 24);

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
          Blog
        </h1>
        <a
          href="/rss.xml"
          className="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-[var(--color-primary)]"
          title="Subscribe via RSS"
        >
          <Rss className="h-4 w-4" aria-hidden="true" /> RSS
        </a>
      </div>

      {posts.items.length === 0 ? (
        <p className="text-slate-600">No posts yet.</p>
      ) : (
        <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {posts.items.map((post) => (
            <li key={post.id}>
              <Link
                href={`/blog/${post.slug}`}
                className="group flex h-full flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white transition-shadow hover:shadow-md"
              >
                {post.featuredImage && (
                  <div className="relative aspect-[16/9] w-full bg-slate-100">
                    <Image
                      src={post.featuredImage}
                      alt=""
                      fill
                      className="object-cover"
                      sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 360px"
                    />
                  </div>
                )}
                <div className="flex flex-1 flex-col p-5">
                  <div className="text-xs text-slate-500">
                    {post.publishedAt && (
                      <time dateTime={post.publishedAt}>
                        {new Date(post.publishedAt).toLocaleDateString(undefined, {
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                        })}
                      </time>
                    )}
                    {post.publishedAt && " · "}
                    {post.readingMinutes} min read
                  </div>
                  <h2 className="mt-1 text-lg font-semibold text-slate-900 group-hover:text-[var(--color-primary)]">
                    {post.title}
                  </h2>
                  {post.excerpt && (
                    <p className="mt-2 line-clamp-3 text-sm text-slate-600">{post.excerpt}</p>
                  )}
                  <div className="mt-auto flex items-center gap-4 pt-4 text-xs text-slate-400">
                    <span className="inline-flex items-center gap-1">
                      <Eye className="h-3.5 w-3.5" aria-hidden="true" />{" "}
                      {post.viewCount.toLocaleString()}
                    </span>
                    <span className="inline-flex items-center gap-1">
                      <Heart className="h-3.5 w-3.5" aria-hidden="true" />{" "}
                      {post.likeCount.toLocaleString()}
                    </span>
                  </div>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
