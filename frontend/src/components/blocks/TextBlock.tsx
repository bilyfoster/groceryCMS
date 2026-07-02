interface TextBlockProps {
  content: Record<string, unknown>;
  className?: string;
}

export function TextBlock({ content, className }: TextBlockProps) {
  const heading = content.heading ? String(content.heading) : null;
  const body = content.body ? String(content.body) : "";
  const alignment = String(content.alignment ?? "left");

  return (
    <section className={className ?? "mx-auto max-w-6xl px-4 py-10"}>
      <div className={`text-${alignment === "center" ? "center" : alignment === "right" ? "right" : "left"}`}>
        {heading && (
          <h2
            className="mb-4 text-2xl font-semibold md:text-3xl"
            style={{ fontFamily: "var(--font-heading)" }}
          >
            {heading}
          </h2>
        )}
        {body && (
          <div
            className="prose prose-slate max-w-none"
            dangerouslySetInnerHTML={{ __html: body }}
          />
        )}
      </div>
    </section>
  );
}
