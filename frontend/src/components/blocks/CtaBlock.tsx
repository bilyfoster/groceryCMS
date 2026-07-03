import Link from "next/link";

export function CtaBlock({ content }: { content: Record<string, unknown> }) {
  const heading = String(content.heading ?? "");
  const body = content.body ? String(content.body) : "";
  const primary = content.primaryButton as { text?: string; url?: string } | undefined;
  const secondary = content.secondaryButton as { text?: string; url?: string } | undefined;

  return (
    <section className="bg-[var(--color-primary)] py-14 text-white md:py-16">
      <div className="mx-auto flex max-w-6xl flex-col items-start gap-5 px-4 md:flex-row md:items-center md:justify-between md:gap-10">
        <div className="max-w-3xl">
          <h2 className="text-2xl font-bold leading-tight md:text-4xl">{heading}</h2>
          {body && <p className="mt-3 max-w-2xl text-base leading-7 text-white/90 md:text-lg">{body}</p>}
        </div>
        <div className="flex w-full flex-col gap-3 sm:w-auto sm:flex-row md:shrink-0">
          {primary?.text && primary?.url && (
            <Link
              href={primary.url}
              className="inline-flex min-h-[46px] items-center justify-center rounded-[var(--border-radius)] bg-white px-6 py-3 text-sm font-semibold text-[var(--color-primary)] shadow-sm"
            >
              {primary.text}
            </Link>
          )}
          {secondary?.text && secondary?.url && (
            <Link
              href={secondary.url}
              className="inline-flex min-h-[46px] items-center justify-center rounded-[var(--border-radius)] border border-white/80 px-6 py-3 text-sm font-semibold text-white"
            >
              {secondary.text}
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
