import type { ComponentType } from "react";
import type { ContentBlock } from "@/types/page";
import { HeroBlock } from "@/components/blocks/HeroBlock";
import { TextBlock } from "@/components/blocks/TextBlock";
import { CtaBlock } from "@/components/blocks/CtaBlock";
import { ImageBlock } from "@/components/blocks/ImageBlock";
import { VideoBlock } from "@/components/blocks/VideoBlock";
import { EmbedBlock } from "@/components/blocks/EmbedBlock";
import { SpacerBlock } from "@/components/blocks/SpacerBlock";
import { DividerBlock } from "@/components/blocks/DividerBlock";
import { QuoteBlock } from "@/components/blocks/QuoteBlock";
import { ButtonBlock } from "@/components/blocks/ButtonBlock";
import { ColumnsBlock } from "@/components/blocks/ColumnsBlock";
import { GroupBlock } from "@/components/blocks/GroupBlock";
import { PricingBlock } from "@/components/blocks/PricingBlock";

const BLOCKS: Record<string, ComponentType<{ content: Record<string, unknown> }>> = {
  hero: HeroBlock,
  text: TextBlock,
  cta: CtaBlock,
  image: ImageBlock,
  video: VideoBlock,
  embed: EmbedBlock,
  spacer: SpacerBlock,
  divider: DividerBlock,
  quote: QuoteBlock,
  button: ButtonBlock,
  columns: ColumnsBlock,
  group: GroupBlock,
  icon_text: TextBlock,
  testimonials: TextBlock,
  pricing: PricingBlock,
  accordion: TextBlock,
  map: EmbedBlock,
  table: TextBlock,
};

export function BlockRenderer({ blocks }: { blocks: ContentBlock[] }) {
  return (
    <>
      {blocks
        .filter((b) => b.published)
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map((block) => {
          const Component = BLOCKS[block.blockType] ?? TextBlock;
          return <Component key={block.id} content={block.content} />;
        })}
    </>
  );
}
