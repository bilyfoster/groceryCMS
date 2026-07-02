import Image from "next/image";

export function ImageBlock({ content }: { content: Record<string, unknown> }) {
  const url = String(content.url ?? "");
  const altText = String(content.altText ?? "");
  const caption = content.caption ? String(content.caption) : null;
  const width = Number(content.width ?? 1200);
  const alignment = String(content.alignment ?? "center");

  if (!url) return null;

  const alignClass =
    alignment === "left"
      ? "mr-auto"
      : alignment === "right"
        ? "ml-auto"
        : "mx-auto";

  return (
    <figure className={`mx-auto max-w-6xl px-4 py-8 ${alignClass}`}>
      <Image
        src={url}
        alt={altText}
        width={width}
        height={Math.round(width * 0.6)}
        className="h-auto w-full max-w-full rounded-[var(--border-radius)]"
        sizes="(max-width: 768px) 100vw, 1200px"
      />
      {caption && (
        <figcaption className="mt-2 text-center text-sm text-slate-500">{caption}</figcaption>
      )}
    </figure>
  );
}
