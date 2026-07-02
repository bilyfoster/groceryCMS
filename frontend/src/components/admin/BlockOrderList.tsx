"use client";

import { ChevronDown, ChevronUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { ContentBlock } from "@/types/page";

interface BlockOrderListProps {
  blocks: ContentBlock[];
  onReorder: (orderedIds: string[]) => void;
}

export function BlockOrderList({ blocks, onReorder }: BlockOrderListProps) {
  const sorted = [...blocks].sort((a, b) => a.sortOrder - b.sortOrder);

  const move = (index: number, direction: -1 | 1) => {
    const next = index + direction;
    if (next < 0 || next >= sorted.length) return;
    const ids = sorted.map((b) => b.id);
    [ids[index], ids[next]] = [ids[next], ids[index]];
    onReorder(ids);
  };

  return (
    <ul className="flex flex-col gap-2">
      {sorted.map((block, index) => (
        <li
          key={block.id}
          className="flex items-center justify-between gap-2 rounded-[var(--border-radius)] border border-slate-200 p-3"
        >
          <span className="text-sm font-medium capitalize">{block.blockType}</span>
          <div className="flex gap-1">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Move up"
              disabled={index === 0}
              onClick={() => move(index, -1)}
            >
              <ChevronUp className="h-5 w-5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Move down"
              disabled={index === sorted.length - 1}
              onClick={() => move(index, 1)}
            >
              <ChevronDown className="h-5 w-5" />
            </Button>
          </div>
        </li>
      ))}
    </ul>
  );
}
