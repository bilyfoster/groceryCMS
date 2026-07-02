import Link from "next/link";
import { notFound } from "next/navigation";
import { fetchBlogByCategory } from "@/lib/api";
import { ApiError } from "@/lib/api";

export default async function BlogCategoryPage({
  params,
}: {
  params: { slug: string };
}) {
  let result;
  try {
    result = await fetchBlogByCategory(params.slug, 0, 24);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) {
      notFound();
    }
    throw e;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-12">
      <h1 className="mb-6 text-3xl font-bold capitalize">
        {params.slug.replace(/-/g, " ")}
      </h1>
      <ul className="space-y-4">
        {result.items.map((post) => (
          <li key={post.id}>
            <Link
              href={`/blog/${post.slug}`}
              className="text-lg font-medium text-[var(--color-primary)] hover:underline"
            >
              {post.title}
              {post.sticky && (
                <span className="ml-2 text-xs font-normal text-amber-600">Pinned</span>
              )}
            </Link>
            {post.excerpt && <p className="mt-1 text-sm text-slate-600">{post.excerpt}</p>}
          </li>
        ))}
      </ul>
      {result.items.length === 0 && (
        <p className="text-slate-600">No posts in this category yet.</p>
      )}
    </div>
  );
}
