import type { ReactNode } from "react";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import { TextBlock } from "@/components/blocks/TextBlock";
import type { ContentBlock } from "@/types/page";
import { cn } from "@/lib/utils";
import { BLOCK_ICONS } from "@/lib/blockIcons";

const gridCols: Record<number, string> = {
  1: "md:grid-cols-1",
  2: "md:grid-cols-2",
  3: "md:grid-cols-3",
  4: "md:grid-cols-4",
};

interface PageBlockRendererProps {
  blocks: ContentBlock[];
  groupClassName?: string;
}

function TextBlockGroup({
  blocks,
  className,
}: {
  blocks: ContentBlock[];
  className?: string;
}) {
  const count = Math.min(blocks.length, 3);
  return (
    <section
      className={cn(
        "mx-auto max-w-6xl px-4 py-10",
        className,
      )}
    >
      <div
        className={cn(
          "grid grid-cols-1 gap-6",
          gridCols[count],
        )}
      >
        {blocks.map((block) => {
          const iconName = block.content?.icon as string | undefined;
          const Icon = iconName ? BLOCK_ICONS[iconName] : undefined;
          return (
            <div
              key={block.id}
              className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-shadow hover:shadow-md"
            >
              {Icon && (
                <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-full bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
                  <Icon className="h-6 w-6" />
                </div>
              )}
              <TextBlock content={block.content} className="h-full" />
            </div>
          );
        })}
      </div>
    </section>
  );
}

export function PageBlockRenderer({ blocks, groupClassName }: PageBlockRendererProps) {
  const sortedBlocks = [...blocks]
    .filter((b) => b.published)
    .sort((a, b) => a.sortOrder - b.sortOrder);

  const children: ReactNode[] = [];
  let i = 0;
  while (i < sortedBlocks.length) {
    const block = sortedBlocks[i];
    if (block.blockType === "text") {
      const run: ContentBlock[] = [];
      while (i < sortedBlocks.length && sortedBlocks[i].blockType === "text") {
        run.push(sortedBlocks[i]);
        i++;
      }
      children.push(
        <TextBlockGroup
          key={run[0].id}
          blocks={run}
          className={groupClassName}
        />,
      );
    } else {
      children.push(<BlockRenderer key={block.id} blocks={[block]} />);
      i++;
    }
  }

  return <>{children}</>;
}
