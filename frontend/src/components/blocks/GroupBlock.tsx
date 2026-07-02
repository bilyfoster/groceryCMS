import type { ContentBlock } from "@/types/page";
import { NestedBlocks } from "@/components/blocks/NestedBlocks";

export function GroupBlock({ content }: { content: Record<string, unknown> }) {
  const blocks = Array.isArray(content.blocks) ? (content.blocks as ContentBlock[]) : [];
  const bg = typeof content.backgroundColor === "string" ? content.backgroundColor : undefined;
  return (
    <section className="py-8" style={bg ? { backgroundColor: bg } : undefined}>
      <div className="mx-auto max-w-6xl px-4">
        <NestedBlocks blocks={blocks} />
      </div>
    </section>
  );
}
