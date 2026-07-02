"use client";

import { useCreateBlockNote } from "@blocknote/react";
import { BlockNoteView } from "@blocknote/mantine";
import "@blocknote/core/fonts/inter.css";
import "@blocknote/mantine/style.css";

interface RichTextEditorProps {
  initialContent?: string;
  onChange: (json: string) => void;
}

function parseInitialContent(raw?: string) {
  if (!raw || raw === "[]") return undefined;
  try {
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed) && parsed.length === 0) return undefined;
    return parsed;
  } catch {
    return undefined;
  }
}

export function RichTextEditor({ initialContent, onChange }: RichTextEditorProps) {
  const editor = useCreateBlockNote({
    initialContent: parseInitialContent(initialContent),
  });

  return (
    <div className="min-h-[200px] rounded-[var(--border-radius)] border border-slate-200 bg-white">
      <BlockNoteView
        editor={editor}
        theme="light"
        onChange={() => onChange(JSON.stringify(editor.document))}
      />
    </div>
  );
}
