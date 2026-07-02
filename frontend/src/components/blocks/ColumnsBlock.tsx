import type { ContentBlock } from "@/types/page";
import { NestedBlocks } from "@/components/blocks/NestedBlocks";
import { cn } from "@/lib/utils";

type Column = { blocks?: ContentBlock[] };

const gridCols: Record<number, string> = {
  1: "md:grid-cols-1",
  2: "md:grid-cols-2",
  3: "md:grid-cols-3",
  4: "md:grid-cols-4",
};

export function ColumnsBlock({ content }: { content: Record<string, unknown> }) {
  const columns = Array.isArray(content.columns) ? (content.columns as Column[]) : [];
  const count = Math.min(columns.length || 2, 4);
  return (
    <div
      className={cn(
        "mx-auto grid max-w-6xl grid-cols-1 gap-8 px-4 py-8",
        gridCols[count],
      )}
    >
      {columns.map((col, i) => (
        <div key={i}>
          <NestedBlocks blocks={col.blocks ?? []} />
        </div>
      ))}
    </div>
  );
}
