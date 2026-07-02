import { PageBlockRenderer } from "@/components/blocks/PageBlockRenderer";
import type { PageTypeProps } from "@/components/page-types";

export function HomePage({ page }: PageTypeProps) {
  const layout = page.layout || "hero-centered";

  return (
    <article data-layout={layout}>
      <PageBlockRenderer blocks={page.blocks} />
    </article>
  );
}
