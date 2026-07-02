import Image from "next/image";
import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { fetchBlogPost, fetchTenantSettings } from "@/lib/api";
import { generatePostMetadata, articleJsonLd } from "@/lib/seo";
import { BlogEngagement } from "@/components/blog/BlogEngagement";

interface Props {
  params: { postSlug: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const [post, tenant] = await Promise.all([
    fetchBlogPost(params.postSlug),
    fetchTenantSettings().catch(() => null),
  ]);
  if (!post) return {};
  return generatePostMetadata(post, tenant);
}

export default async function BlogPostPage({ params }: Props) {
  const [post, tenant] = await Promise.all([
    fetchBlogPost(params.postSlug),
    fetchTenantSettings().catch(() => null),
  ]);

  if (!post) notFound();

  const schema = articleJsonLd(post, tenant);

  return (
    <article className="mx-auto max-w-3xl px-4 py-8">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(schema) }}
      />
      <h1 className="mb-3 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
        {post.title}
      </h1>
      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-slate-500">
        {post.publishedAt && (
          <time dateTime={post.publishedAt}>
            {new Date(post.publishedAt).toLocaleDateString(undefined, {
              year: "numeric",
              month: "long",
              day: "numeric",
            })}
          </time>
        )}
        {post.publishedAt && <span aria-hidden="true">·</span>}
        <span>{post.readingMinutes} min read</span>
      </div>
      <div className="mt-4 border-b border-slate-200 pb-4">
        <BlogEngagement
          slug={post.slug}
          initialViews={post.viewCount}
          initialLikes={post.likeCount}
        />
      </div>
      {post.featuredImage && (
        <div className="relative mt-6 aspect-[16/9] w-full overflow-hidden rounded-2xl bg-slate-100">
          <Image
            src={post.featuredImage}
            alt=""
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, 768px"
            priority
          />
        </div>
      )}
      <div
        className="prose prose-slate mt-8 max-w-none"
        dangerouslySetInnerHTML={{ __html: post.bodyHtml }}
      />
    </article>
  );
}
