import type { ContentBlock } from "@/types/page";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";

export function NestedBlocks({ blocks }: { blocks: ContentBlock[] }) {
  if (!blocks?.length) return null;
  return <BlockRenderer blocks={blocks} />;
}
