import Link from "next/link";

export function ButtonBlock({ content }: { content: Record<string, unknown> }) {
  const text = typeof content.text === "string" ? content.text : "Learn more";
  const url = typeof content.url === "string" ? content.url : "#";
  const style = content.style === "outline" ? "outline" : "primary";
  const className =
    style === "outline"
      ? "inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] border border-[var(--color-primary)] px-6 py-2 text-[var(--color-primary)]"
      : "inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-6 py-2 text-white";
  return (
    <div className="flex justify-center px-4 py-6">
      <Link href={url} className={className}>
        {text}
      </Link>
    </div>
  );
}

