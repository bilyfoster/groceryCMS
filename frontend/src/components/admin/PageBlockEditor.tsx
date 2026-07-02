"use client";

import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { HtmlRichTextEditor } from "@/components/admin/HtmlRichTextEditor";
import { ImageInput } from "@/components/admin/ImageInput";
import { BLOCK_ICON_NAMES } from "@/lib/blockIcons";
import type { ContentBlock } from "@/types/page";

const BLOCK_TYPES = [
  { value: "hero", label: "Hero" },
  { value: "text", label: "Text" },
  { value: "cta", label: "Call to action" },
  { value: "image", label: "Image" },
  { value: "video", label: "Video" },
  { value: "embed", label: "Embed" },
  { value: "columns", label: "Columns" },
  { value: "group", label: "Group" },
  { value: "spacer", label: "Spacer" },
  { value: "divider", label: "Divider" },
  { value: "quote", label: "Quote" },
  { value: "button", label: "Button" },
  { value: "pricing", label: "Pricing" },
] as const;

function defaultContent(blockType: string): Record<string, unknown> {
  switch (blockType) {
    case "hero":
      return {
        heading: "Heading",
        subheading: "",
        buttonText: "",
        buttonUrl: "",
        backgroundImage: "",
        overlay: true,
      };
    case "text":
      return { heading: "", body: "<p>Your content here</p>", alignment: "left" };
    case "cta":
      return {
        heading: "Ready to get started?",
        body: "",
        primaryButton: { text: "Contact us", url: "/contact" },
        secondaryButton: { text: "", url: "" },
      };
    case "image":
      return { url: "", altText: "", caption: "", width: "full", alignment: "center" };
    case "video":
      return { embedUrl: "", title: "", aspectRatio: "16/9" };
    case "embed":
      return { html: "", height: 400 };
    case "columns":
      return { columns: [{ blocks: [] }, { blocks: [] }] };
    case "group":
      return { blocks: [], backgroundColor: "" };
    case "spacer":
      return { height: 48 };
    case "divider":
      return {};
    case "quote":
      return { quote: "", attribution: "" };
    case "button":
      return { text: "Learn more", url: "/", style: "primary" };
    case "pricing":
      return {
        heading: "Services & Pricing",
        intro: "",
        note: "",
        tiers: [
          { name: "Individual Therapy", price: "Intake: $235\nSession: $180", featured: false },
        ],
        insurance: [],
        paymentNote: "",
        ctaText: "Book Now",
        ctaUrl: "",
      };
    default:
      return {};
  }
}

interface PageBlockEditorProps {
  blocks: ContentBlock[];
  onChange: (blocks: ContentBlock[]) => void;
  onAdd: (blockType: string) => void;
  onRemove: (blockId: string) => void;
  onMove: (orderedIds: string[]) => void;
}

export function PageBlockEditor({
  blocks,
  onChange,
  onAdd,
  onRemove,
  onMove,
}: PageBlockEditorProps) {
  const sorted = [...blocks].sort((a, b) => a.sortOrder - b.sortOrder);

  const updateBlockContent = (id: string, content: Record<string, unknown>) => {
    onChange(blocks.map((b) => (b.id === id ? { ...b, content } : b)));
  };

  const move = (index: number, direction: -1 | 1) => {
    const next = index + direction;
    if (next < 0 || next >= sorted.length) return;
    const ids = sorted.map((b) => b.id);
    [ids[index], ids[next]] = [ids[next], ids[index]];
    onMove(ids);
  };

  return (
    <div className="flex flex-col gap-4">
      {sorted.map((block, index) => (
        <div
          key={block.id}
          className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-4 shadow-sm"
        >
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <span className="text-sm font-semibold capitalize text-slate-700">
              {block.blockType} block
            </span>
            <div className="flex gap-1">
              <Button
                type="button"
                variant="ghost"
                size="sm"
                disabled={index === 0}
                onClick={() => move(index, -1)}
              >
                ↑
              </Button>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                disabled={index === sorted.length - 1}
                onClick={() => move(index, 1)}
              >
                ↓
              </Button>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => onRemove(block.id)}
                aria-label="Remove block"
              >
                <Trash2 className="h-4 w-4 text-red-600" />
              </Button>
            </div>
          </div>
          <BlockFields
            block={block}
            onContentChange={(content) => updateBlockContent(block.id, content)}
          />
        </div>
      ))}

      <div className="flex flex-wrap gap-2">
        {BLOCK_TYPES.map(({ value, label }) => (
          <Button
            key={value}
            type="button"
            variant="outline"
            size="sm"
            onClick={() => onAdd(value)}
          >
            <Plus className="mr-1 h-4 w-4" />
            {label}
          </Button>
        ))}
      </div>
    </div>
  );
}

function BlockFields({
  block,
  onContentChange,
}: {
  block: ContentBlock;
  onContentChange: (content: Record<string, unknown>) => void;
}) {
  const c = block.content;
  const set = (key: string, value: unknown) =>
    onContentChange({ ...c, [key]: value });

  switch (block.blockType) {
    case "hero":
      return (
        <div className="flex flex-col gap-4">
          <Field label="Heading" value={String(c.heading ?? "")} onChange={(v) => set("heading", v)} />
          <div>
            <Label htmlFor={`subheading-${block.id}`}>Subheading</Label>
            <Textarea
              id={`subheading-${block.id}`}
              rows={2}
              value={String(c.subheading ?? "")}
              onChange={(e) => set("subheading", e.target.value)}
            />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Field
              label="Button text"
              value={String(c.buttonText ?? "")}
              onChange={(v) => set("buttonText", v)}
            />
            <Field
              label="Button URL"
              value={String(c.buttonUrl ?? "")}
              onChange={(v) => set("buttonUrl", v)}
            />
          </div>
          <ImageInput
            label="Background image"
            value={String(c.backgroundImage ?? "")}
            onChange={(v) => set("backgroundImage", v)}
          />
        </div>
      );
    case "text":
      return (
        <div className="flex flex-col gap-4">
          <Field label="Heading" value={String(c.heading ?? "")} onChange={(v) => set("heading", v)} />
          <div>
            <Label className="mb-1 block">Body</Label>
            <HtmlRichTextEditor
              initialHtml={String(c.body ?? "")}
              onChange={(v) => set("body", v)}
            />
          </div>
          <div>
            <Label htmlFor={`align-${block.id}`}>Alignment</Label>
            <select
              id={`align-${block.id}`}
              value={String(c.alignment ?? "left")}
              onChange={(e) => set("alignment", e.target.value)}
              className="flex min-h-[44px] w-full rounded-[var(--border-radius)] border border-slate-300 px-3"
            >
              <option value="left">Left</option>
              <option value="center">Center</option>
              <option value="right">Right</option>
            </select>
          </div>
          <div>
            <Label htmlFor={`icon-${block.id}`}>Icon (optional — shows on card grids)</Label>
            <select
              id={`icon-${block.id}`}
              value={String(c.icon ?? "")}
              onChange={(e) => set("icon", e.target.value)}
              className="flex min-h-[44px] w-full rounded-[var(--border-radius)] border border-slate-300 px-3"
            >
              <option value="">None</option>
              {BLOCK_ICON_NAMES.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
          </div>
        </div>
      );
    case "cta": {
      const primary = (c.primaryButton as { text?: string; url?: string }) ?? {};
      return (
        <div className="flex flex-col gap-3">
          <Field label="Heading" value={String(c.heading ?? "")} onChange={(v) => set("heading", v)} />
          <Field label="Body" value={String(c.body ?? "")} onChange={(v) => set("body", v)} />
          <Field
            label="Primary button text"
            value={String(primary.text ?? "")}
            onChange={(v) => set("primaryButton", { ...primary, text: v })}
          />
          <Field
            label="Primary button URL"
            value={String(primary.url ?? "")}
            onChange={(v) => set("primaryButton", { ...primary, url: v })}
          />
        </div>
      );
    }
    case "image":
      return (
        <div className="flex flex-col gap-3">
          <ImageInput label="Image" value={String(c.url ?? "")} onChange={(v) => set("url", v)} />
          <Field label="Alt text" value={String(c.altText ?? "")} onChange={(v) => set("altText", v)} />
          <Field label="Caption" value={String(c.caption ?? "")} onChange={(v) => set("caption", v)} />
        </div>
      );
    case "video":
      return (
        <div className="flex flex-col gap-3">
          <Field label="Embed URL (YouTube/Vimeo)" value={String(c.embedUrl ?? "")} onChange={(v) => set("embedUrl", v)} />
          <Field label="Title" value={String(c.title ?? "")} onChange={(v) => set("title", v)} />
        </div>
      );
    case "embed":
      return (
        <div>
          <Label htmlFor={`embed-${block.id}`}>Embed HTML</Label>
          <Textarea
            id={`embed-${block.id}`}
            rows={4}
            value={String(c.html ?? "")}
            onChange={(e) => set("html", e.target.value)}
          />
        </div>
      );
    case "divider":
      return (
        <div className="py-2">
          <hr className="border-slate-300" />
          <p className="mt-2 text-xs text-slate-500">
            Use the arrows above to move this divider between other blocks.
          </p>
        </div>
      );
    case "spacer":
      return (
        <div className="flex flex-col gap-2">
          <Label htmlFor={`spacer-${block.id}`}>Height (px)</Label>
          <Input
            id={`spacer-${block.id}`}
            type="number"
            value={Number(c.height ?? 48)}
            onChange={(e) => set("height", Number(e.target.value))}
          />
        </div>
      );
    case "quote":
      return (
        <div className="flex flex-col gap-3">
          <Field label="Quote" value={String(c.quote ?? "")} onChange={(v) => set("quote", v)} />
          <Field label="Attribution" value={String(c.attribution ?? "")} onChange={(v) => set("attribution", v)} />
        </div>
      );
    case "button":
      return (
        <div className="flex flex-col gap-3">
          <Field label="Text" value={String(c.text ?? "")} onChange={(v) => set("text", v)} />
          <Field label="URL" value={String(c.url ?? "")} onChange={(v) => set("url", v)} />
          <div>
            <Label htmlFor={`button-style-${block.id}`}>Style</Label>
            <select
              id={`button-style-${block.id}`}
              value={String(c.style ?? "primary")}
              onChange={(e) => set("style", e.target.value)}
              className="flex min-h-[44px] w-full rounded-[var(--border-radius)] border border-slate-300 px-3"
            >
              <option value="primary">Primary</option>
              <option value="secondary">Secondary</option>
              <option value="outline">Outline</option>
            </select>
          </div>
        </div>
      );
    case "pricing": {
      const tiers = (Array.isArray(c.tiers) ? c.tiers : []) as Array<{
        name?: string;
        description?: string;
        price?: string;
        featured?: boolean;
        image?: string;
        imageAlt?: string;
      }>;
      const insurance = (Array.isArray(c.insurance) ? c.insurance : []) as string[];
      const setTier = (idx: number, patch: Record<string, unknown>) =>
        set("tiers", tiers.map((t, i) => (i === idx ? { ...t, ...patch } : t)));
      return (
        <div className="flex flex-col gap-4">
          <Field label="Heading" value={String(c.heading ?? "")} onChange={(v) => set("heading", v)} />
          <div>
            <Label htmlFor={`intro-${block.id}`}>Intro</Label>
            <Textarea
              id={`intro-${block.id}`}
              rows={3}
              value={String(c.intro ?? "")}
              onChange={(e) => set("intro", e.target.value)}
            />
          </div>
          <Field
            label="Note (small highlighted callout)"
            value={String(c.note ?? "")}
            onChange={(v) => set("note", v)}
          />

          <div className="rounded-[var(--border-radius)] border border-slate-200 bg-slate-50 p-3">
            <Label className="mb-2 block font-semibold">Services</Label>
            <div className="flex flex-col gap-3">
              {tiers.map((t, idx) => (
                <div key={idx} className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-3">
                  <div className="mb-2 flex items-center justify-between">
                    <span className="text-xs font-medium text-slate-500">Service {idx + 1}</span>
                    <button
                      type="button"
                      aria-label="Remove service"
                      onClick={() => set("tiers", tiers.filter((_, i) => i !== idx))}
                      className="text-slate-400 hover:text-red-600"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                  <Field label="Name" value={String(t.name ?? "")} onChange={(v) => setTier(idx, { name: v })} />
                  <ImageInput
                    label="Photo"
                    value={String(t.image ?? "")}
                    onChange={(v) => setTier(idx, { image: v })}
                  />
                  <Field
                    label="Photo alt text"
                    value={String(t.imageAlt ?? "")}
                    onChange={(v) => setTier(idx, { imageAlt: v })}
                  />
                  <div className="mt-2">
                    <Label htmlFor={`description-${block.id}-${idx}`}>Short description</Label>
                    <Input
                      id={`description-${block.id}-${idx}`}
                      value={String(t.description ?? "")}
                      onChange={(e) => setTier(idx, { description: e.target.value })}
                    />
                  </div>
                  <div className="mt-2">
                    <Label htmlFor={`price-${block.id}-${idx}`}>Prices (one per line, “Label: Amount”)</Label>
                    <Textarea
                      id={`price-${block.id}-${idx}`}
                      rows={3}
                      value={String(t.price ?? "")}
                      onChange={(e) => setTier(idx, { price: e.target.value })}
                    />
                  </div>
                  <label className="mt-2 flex items-center gap-2 text-sm text-slate-700">
                    <input
                      type="checkbox"
                      checked={Boolean(t.featured)}
                      onChange={(e) => setTier(idx, { featured: e.target.checked })}
                    />
                    Highlight this service (reduced rates)
                  </label>
                </div>
              ))}
            </div>
            <Button
              variant="outline"
              className="mt-3"
              onClick={() =>
                set("tiers", [...tiers, { name: "New service", price: "Session: $0", featured: false }])
              }
            >
              <Plus className="mr-1 h-4 w-4" /> Add service
            </Button>
          </div>

          <div>
            <Label htmlFor={`insurance-${block.id}`}>Insurance accepted (comma-separated)</Label>
            <Input
              id={`insurance-${block.id}`}
              value={insurance.join(", ")}
              onChange={(e) =>
                set(
                  "insurance",
                  e.target.value.split(",").map((s) => s.trim()).filter(Boolean),
                )
              }
            />
          </div>
          <div>
            <Label htmlFor={`paynote-${block.id}`}>Payment note</Label>
            <Textarea
              id={`paynote-${block.id}`}
              rows={2}
              value={String(c.paymentNote ?? "")}
              onChange={(e) => set("paymentNote", e.target.value)}
            />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Field label="Button text" value={String(c.ctaText ?? "")} onChange={(v) => set("ctaText", v)} />
            <Field label="Button URL" value={String(c.ctaUrl ?? "")} onChange={(v) => set("ctaUrl", v)} />
          </div>
        </div>
      );
    }
    default:
      return (
        <p className="text-sm text-slate-500">
          Unknown block type. Content is stored as JSON.
        </p>
      );
  }
}

function Field({
  label,
  value,
  onChange,
  id,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  id?: string;
}) {
  const inputId = id || label.replace(/\s+/g, "-").toLowerCase();
  return (
    <div>
      <Label htmlFor={inputId}>{label}</Label>
      <Input id={inputId} value={value} onChange={(e) => onChange(e.target.value)} />
    </div>
  );
}

export { defaultContent, BLOCK_TYPES };
