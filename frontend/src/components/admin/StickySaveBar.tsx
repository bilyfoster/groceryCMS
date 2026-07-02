"use client";

import { Button } from "@/components/ui/button";

interface StickySaveBarProps {
  onSave: () => void;
  saving?: boolean;
  label?: string;
}

export function StickySaveBar({
  onSave,
  saving,
  label = "Save",
}: StickySaveBarProps) {
  return (
    <div className="fixed bottom-16 left-0 right-0 z-30 border-t border-slate-200 bg-white/95 p-3 backdrop-blur lg:bottom-0 lg:left-56">
      <div className="mx-auto flex max-w-4xl justify-end">
        <Button type="button" onClick={onSave} disabled={saving}>
          {saving ? "Saving…" : label}
        </Button>
      </div>
    </div>
  );
}
