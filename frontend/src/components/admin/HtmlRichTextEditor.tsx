"use client";

import { useEffect, useRef, useState } from "react";
import {
  Bold,
  Italic,
  Underline,
  List,
  ListOrdered,
  Link as LinkIcon,
  Unlink,
  Heading1,
  Heading2,
} from "lucide-react";
import { Button } from "@/components/ui/button";

interface HtmlRichTextEditorProps {
  initialHtml?: string;
  onChange: (html: string) => void;
}

function exec(cmd: string, value: string | undefined = undefined) {
  if (typeof document === "undefined") return;
  document.execCommand(cmd, false, value);
}

export function HtmlRichTextEditor({ initialHtml = "", onChange }: HtmlRichTextEditorProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [isFocused, setIsFocused] = useState(false);

  // Sync external changes only when the editor is not focused so we don't
  // clobber the cursor while the user is typing.
  useEffect(() => {
    const el = ref.current;
    if (!el || isFocused) return;
    if (el.innerHTML !== initialHtml) {
      el.innerHTML = initialHtml || "";
    }
  }, [initialHtml, isFocused]);

  const emit = () => {
    if (ref.current) {
      onChange(ref.current.innerHTML);
    }
  };

  const toggleLink = () => {
    const selection = window.getSelection()?.toString();
    const url = window.prompt("URL", selection ? `https://${selection}` : "https://");
    if (url) {
      exec("createLink", url);
      emit();
    }
  };

  const ToolbarButton = ({
    icon: Icon,
    label,
    onClick,
  }: {
    icon: React.ComponentType<{ className?: string }>;
    label: string;
    onClick: () => void;
  }) => (
    <Button
      type="button"
      variant="ghost"
      size="sm"
      onClick={onClick}
      aria-label={label}
      title={label}
      className="h-8 w-8 p-0"
    >
      <Icon className="h-4 w-4" />
    </Button>
  );

  return (
    <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white">
      <div className="flex flex-wrap items-center gap-1 border-b border-slate-200 px-2 py-1">
        <ToolbarButton icon={Bold} label="Bold" onClick={() => { exec("bold"); emit(); }} />
        <ToolbarButton icon={Italic} label="Italic" onClick={() => { exec("italic"); emit(); }} />
        <ToolbarButton icon={Underline} label="Underline" onClick={() => { exec("underline"); emit(); }} />
        <div className="mx-1 h-4 w-px bg-slate-200" />
        <ToolbarButton icon={List} label="Bullet list" onClick={() => { exec("insertUnorderedList"); emit(); }} />
        <ToolbarButton icon={ListOrdered} label="Numbered list" onClick={() => { exec("insertOrderedList"); emit(); }} />
        <div className="mx-1 h-4 w-px bg-slate-200" />
        <ToolbarButton icon={Heading1} label="Heading 1" onClick={() => { exec("formatBlock", "H1"); emit(); }} />
        <ToolbarButton icon={Heading2} label="Heading 2" onClick={() => { exec("formatBlock", "H2"); emit(); }} />
        <div className="mx-1 h-4 w-px bg-slate-200" />
        <ToolbarButton icon={LinkIcon} label="Link" onClick={toggleLink} />
        <ToolbarButton icon={Unlink} label="Remove link" onClick={() => { exec("unlink"); emit(); }} />
      </div>
      <div
        ref={ref}
        className="min-h-[160px] p-3 text-sm outline-none prose prose-slate max-w-none"
        contentEditable
        suppressContentEditableWarning
        onFocus={() => setIsFocused(true)}
        onBlur={() => {
          setIsFocused(false);
          emit();
        }}
        onInput={emit}
      />
    </div>
  );
}
