import { notFound } from "next/navigation";
import { fetchFrontPage, fetchTenantSettings } from "@/lib/api";
import { generatePageMetadata } from "@/lib/seo";
import { HomePage } from "@/components/page-types/HomePage";
import type { Metadata } from "next";

export async function generateMetadata(): Promise<Metadata> {
  const [page, tenant] = await Promise.all([
    fetchFrontPage(),
    fetchTenantSettings().catch(() => null),
  ]);
  if (!page) return {};
  return generatePageMetadata(page, tenant);
}

export default async function Home() {
  const page = await fetchFrontPage();
  if (!page || !page.published) notFound();
  return <HomePage page={page} />;
}
