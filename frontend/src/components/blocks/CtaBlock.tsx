import Link from "next/link";

export function CtaBlock({ content }: { content: Record<string, unknown> }) {
  const heading = String(content.heading ?? "");
  const body = content.body ? String(content.body) : "";
  const primary = content.primaryButton as { text?: string; url?: string } | undefined;
  const secondary = content.secondaryButton as { text?: string; url?: string } | undefined;

  return (
    <section className="bg-[var(--color-primary)] py-12 text-white">
      <div className="mx-auto flex max-w-6xl flex-col items-center gap-4 px-4 text-center">
        <h2 className="text-2xl font-bold md:text-3xl">{heading}</h2>
        {body && <p className="max-w-2xl text-lg opacity-90">{body}</p>}
        <div className="flex flex-col gap-3 sm:flex-row">
          {primary?.text && primary?.url && (
            <Link
              href={primary.url}
              className="inline-flex min-h-[44px] items-center justify-center rounded-[var(--border-radius)] bg-white px-6 py-2 text-sm font-medium text-[var(--color-primary)]"
            >
              {primary.text}
            </Link>
          )}
          {secondary?.text && secondary?.url && (
            <Link
              href={secondary.url}
              className="inline-flex min-h-[44px] items-center justify-center rounded-[var(--border-radius)] border border-white px-6 py-2 text-sm font-medium text-white"
            >
              {secondary.text}
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
