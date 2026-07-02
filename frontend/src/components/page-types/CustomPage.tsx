import { PageBlockRenderer } from "@/components/blocks/PageBlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import { cn } from "@/lib/utils";

export function CustomPage({ page }: PageTypeProps) {
  const layout = page.layout || "contained";

  const wrapperClass = cn(
    "mx-auto px-4 py-8",
    layout === "full-width" && "max-w-none px-0",
    layout === "contained" && "max-w-6xl",
    (layout === "sidebar-left" || layout === "sidebar-right") &&
      "max-w-6xl md:grid md:grid-cols-[240px_1fr] md:gap-8",
    layout === "sidebar-right" && "md:grid-cols-[1fr_240px]",
  );

  return (
    <article className={wrapperClass} data-layout={layout}>
      {(layout === "sidebar-left" || layout === "sidebar-right") && (
        <aside className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4 md:mb-0">
          <p className="text-sm text-slate-600">{page.title}</p>
        </aside>
      )}
      <div>
        <PageBlockRenderer blocks={page.blocks} groupClassName="py-8" />
      </div>
    </article>
  );
}
