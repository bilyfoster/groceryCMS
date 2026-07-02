export function QuoteBlock({ content }: { content: Record<string, unknown> }) {
  const quote = typeof content.quote === "string" ? content.quote : "";
  const attribution = typeof content.attribution === "string" ? content.attribution : "";
  return (
    <blockquote className="mx-auto max-w-3xl border-l-4 border-[var(--color-primary)] px-6 py-2 italic text-slate-700">
      <p>{quote}</p>
      {attribution && <footer className="mt-2 text-sm not-italic text-slate-500">— {attribution}</footer>}
    </blockquote>
  );
}
