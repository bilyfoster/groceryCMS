export function VideoBlock({ content }: { content: Record<string, unknown> }) {
  const embedUrl = String(content.embedUrl ?? "");
  const title = String(content.title ?? "Video");
  const aspectRatio = String(content.aspectRatio ?? "16/9");

  if (!embedUrl) return null;

  return (
    <section className="mx-auto max-w-6xl px-4 py-8">
      <div
        className="relative w-full overflow-hidden rounded-[var(--border-radius)]"
        style={{ aspectRatio }}
      >
        <iframe
          src={embedUrl}
          title={title}
          className="absolute inset-0 h-full w-full border-0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
        />
      </div>
    </section>
  );
}
