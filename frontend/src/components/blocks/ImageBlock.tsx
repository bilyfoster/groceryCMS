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
    <figure className={`mx-auto max-w-6xl px-4 py-8 md:py-10 ${alignClass}`}>
      <div className="overflow-hidden rounded-[var(--border-radius)] border border-slate-200 bg-slate-100 shadow-sm">
        <Image
          src={url}
          alt={altText}
          width={width}
          height={Math.round(width * 0.58)}
          className="aspect-[16/9] h-auto w-full max-w-full object-cover"
          sizes="(max-width: 768px) 100vw, 1200px"
        />
      </div>
      {caption && (
        <figcaption className="mx-auto mt-3 max-w-2xl text-center text-sm leading-6 text-slate-500">{caption}</figcaption>
      )}
    </figure>
  );
}
