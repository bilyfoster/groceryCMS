import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { fetchPage, fetchTenantSettings } from "@/lib/api";
import { generatePageMetadata } from "@/lib/seo";
import { PAGE_TYPE_COMPONENTS } from "@/components/page-types";
import { PagePasswordGate } from "@/components/page/PagePasswordGate";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import type { PageType } from "@/types/page";

const RESERVED = new Set(["blog", "admin", "auth", "api", "search"]);

interface Props {
  params: { slug: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  if (RESERVED.has(params.slug)) return {};
  const [page, tenant] = await Promise.all([
    fetchPage(params.slug),
    fetchTenantSettings().catch(() => null),
  ]);
  if (!page) return {};
  return generatePageMetadata(page, tenant);
}

export default async function DynamicPage({ params }: Props) {
  if (RESERVED.has(params.slug)) notFound();

  const page = await fetchPage(params.slug);
  if (!page || !page.published) notFound();

  if (page.passwordProtected && page.blocks.length === 0) {
    return <PagePasswordGate slug={params.slug} title={page.title} />;
  }

  const pageType = page.pageType as PageType;
  const PageComponent =
    PAGE_TYPE_COMPONENTS[pageType] ?? PAGE_TYPE_COMPONENTS.custom;

  return (
    <>
      <AnalyticsTracker eventType="page_view" metadata={{ path: params.slug, pageType }} />
      <PageComponent page={page} />
    </>
  );
}
