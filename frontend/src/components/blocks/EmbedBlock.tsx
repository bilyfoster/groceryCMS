export function EmbedBlock({ content }: { content: Record<string, unknown> }) {
  const html = content.html ? String(content.html) : "";
  const height = Number(content.height ?? 400);

  if (!html) return null;

  return (
    <section className="mx-auto max-w-6xl px-4 py-8">
      <div
        className="w-full overflow-hidden rounded-[var(--border-radius)] border border-slate-200"
        style={{ minHeight: height }}
        dangerouslySetInnerHTML={{ __html: html }}
      />
    </section>
  );
}
