import { fetchNotFoundPage, fetchTenantSettings } from "@/lib/api";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { Metadata } from "next";

export async function generateMetadata(): Promise<Metadata> {
  const page = await fetchNotFoundPage().catch(() => null);
  return { title: page?.metaTitle ?? "Page not found" };
}

export default async function NotFound() {
  const page = await fetchNotFoundPage().catch(() => null);
  const tenant = await fetchTenantSettings().catch(() => null);

  if (page?.blocks?.length) {
    return (
      <article className="py-12">
        <BlockRenderer blocks={page.blocks} />
      </article>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-24 text-center">
      <h1 className="text-3xl font-bold text-slate-900">Page not found</h1>
      <p className="mt-2 text-slate-600">
        {tenant?.name ? `We could not find that page on ${tenant.name}.` : "We could not find that page."}
      </p>
    </div>
  );
}
