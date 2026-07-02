import Link from "next/link";
import { searchSite } from "@/lib/api";

export const dynamic = "force-dynamic";

export default async function SearchPage({
  searchParams,
}: {
  searchParams: { q?: string };
}) {
  const q = searchParams.q?.trim() ?? "";
  const results = q ? await searchSite(q).catch(() => ({ pages: [], posts: [] })) : { pages: [], posts: [] };

  return (
    <div className="mx-auto max-w-3xl px-4 py-12">
      <h1 className="mb-6 text-3xl font-bold">Search</h1>
      <form method="get" className="mb-8">
        <input
          type="search"
          name="q"
          defaultValue={q}
          placeholder="Search pages and posts…"
          className="w-full rounded-lg border border-slate-300 px-4 py-3"
        />
      </form>
      {q && (
        <>
          {results.pages.length > 0 && (
            <section className="mb-8">
              <h2 className="mb-3 text-lg font-semibold">Pages</h2>
              <ul className="space-y-2">
                {results.pages.map((hit) => (
                  <li key={hit.id}>
                    <Link href={hit.href} className="font-medium text-[var(--color-primary)] hover:underline">
                      {hit.title}
                    </Link>
                    {hit.excerpt && <p className="text-sm text-slate-600">{hit.excerpt}</p>}
                  </li>
                ))}
              </ul>
            </section>
          )}
          {results.posts.length > 0 && (
            <section>
              <h2 className="mb-3 text-lg font-semibold">Blog posts</h2>
              <ul className="space-y-2">
                {results.posts.map((hit) => (
                  <li key={hit.id}>
                    <Link href={hit.href} className="font-medium text-[var(--color-primary)] hover:underline">
                      {hit.title}
                    </Link>
                    {hit.excerpt && <p className="text-sm text-slate-600">{hit.excerpt}</p>}
                  </li>
                ))}
              </ul>
            </section>
          )}
          {results.pages.length === 0 && results.posts.length === 0 && (
            <p className="text-slate-600">No results for &ldquo;{q}&rdquo;.</p>
          )}
        </>
      )}
    </div>
  );
}
