"use client";

import { useQuery } from "@tanstack/react-query";
import { fetchBlockPatterns } from "@/lib/api";
import { Button } from "@/components/ui/button";

interface PatternPickerProps {
  onInsert: (blocks: Array<{ blockType: string; content: Record<string, unknown> }>) => void;
}

export function PatternPicker({ onInsert }: PatternPickerProps) {
  const { data: patterns = [] } = useQuery({
    queryKey: ["block-patterns"],
    queryFn: fetchBlockPatterns,
  });

  const byCategory = patterns.reduce<Record<string, typeof patterns>>((acc, p) => {
    (acc[p.category] ??= []).push(p);
    return acc;
  }, {});

  if (patterns.length === 0) return null;

  return (
    <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
      <h3 className="mb-3 text-sm font-semibold text-slate-700">Block patterns</h3>
      <div className="flex flex-col gap-4">
        {Object.entries(byCategory).map(([category, items]) => (
          <div key={category}>
            <p className="mb-2 text-xs font-medium uppercase tracking-wide text-slate-500">
              {category}
            </p>
            <div className="flex flex-wrap gap-2">
              {items.map((pattern) => (
                <Button
                  key={pattern.id}
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => onInsert(pattern.blocks)}
                >
                  {pattern.name}
                </Button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
