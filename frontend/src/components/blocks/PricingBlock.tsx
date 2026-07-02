import Image from "next/image";
import Link from "next/link";

type PriceRow = { label: string; amount: string };
type Tier = {
  name?: string;
  description?: string;
  price?: string; // "Label: Amount" per line (CMS-friendly)
  prices?: PriceRow[]; // legacy structured form
  featured?: boolean;
  image?: string;
  imageAlt?: string;
};

/** Parse the CMS-friendly "Label: Amount" (one per line) into rows. */
function parsePrice(tier: Tier): PriceRow[] {
  if (Array.isArray(tier.prices) && tier.prices.length > 0) return tier.prices;
  if (!tier.price) return [];
  return tier.price
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const i = line.indexOf(":");
      return i === -1
        ? { label: "", amount: line.replace(/^[-•\s]+/, "") }
        : { label: line.slice(0, i).replace(/^[-•\s]+/, "").trim(), amount: line.slice(i + 1).trim() };
    });
}

export function PricingBlock({ content }: { content: Record<string, unknown> }) {
  const heading = content.heading ? String(content.heading) : "";
  const intro = content.intro ? String(content.intro) : "";
  const note = content.note ? String(content.note) : "";
  const tiers = (Array.isArray(content.tiers) ? content.tiers : []) as Tier[];
  const insurance = (Array.isArray(content.insurance) ? content.insurance : []) as string[];
  const paymentNote = content.paymentNote ? String(content.paymentNote) : "";
  const ctaText = content.ctaText ? String(content.ctaText) : "";
  const ctaUrl = content.ctaUrl ? String(content.ctaUrl) : "";

  return (
    <section className="mx-auto max-w-6xl px-4 py-12 md:py-16" aria-labelledby="pricing-heading">
      {(heading || intro || note) && (
        <div className="mx-auto mb-10 max-w-2xl text-center">
          {heading && (
            <h2
              id="pricing-heading"
              className="text-3xl font-bold text-slate-900"
              style={{ fontFamily: "var(--font-heading)" }}
            >
              {heading}
            </h2>
          )}
          {intro && <p className="mt-4 text-lg leading-relaxed text-slate-600">{intro}</p>}
          {note && (
            <p className="mt-5 inline-flex items-center gap-2 rounded-full border border-amber-200 bg-amber-50 px-4 py-1.5 text-sm font-medium text-amber-900">
              <span aria-hidden="true">ⓘ</span>
              {note}
            </p>
          )}
        </div>
      )}

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {tiers.map((tier, i) => {
          const rows = parsePrice(tier);
          return (
            <div
              key={tier.name ?? i}
              className={`relative flex flex-col overflow-hidden rounded-2xl bg-white transition-shadow hover:shadow-md ${
                tier.featured
                  ? "border-2 border-[var(--color-primary)] shadow-sm"
                  : "border border-slate-200"
              }`}
            >
              {tier.featured && (
                <div className="bg-[var(--color-primary)] py-1 text-center text-xs font-semibold uppercase tracking-wide text-white">
                  Reduced rates
                </div>
              )}
              {tier.image && (
                <div className="relative aspect-[3/2] w-full">
                  <Image src={tier.image} alt={tier.imageAlt ?? ""} fill className="object-cover" sizes="300px" />
                </div>
              )}
              <div className="flex flex-1 flex-col p-6">
                {tier.name && <h3 className="text-lg font-semibold text-slate-900">{tier.name}</h3>}
                {tier.description && <p className="mt-1 text-sm text-slate-600">{tier.description}</p>}
                {rows.length > 0 && (
                  <dl className="mt-4 space-y-2 border-t border-slate-100 pt-4">
                    {rows.map((r, j) => (
                      <div key={j} className="flex items-baseline justify-between gap-3">
                        {r.label && <dt className="text-sm text-slate-500">{r.label}</dt>}
                        <dd className={`font-semibold text-slate-900 ${r.label ? "" : "w-full"}`}>{r.amount}</dd>
                      </div>
                    ))}
                  </dl>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {(insurance.length > 0 || paymentNote) && (
        <div className="mx-auto mt-10 max-w-3xl rounded-2xl bg-slate-50 p-6 text-center">
          {insurance.length > 0 && (
            <>
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Insurance accepted (in-network)
              </p>
              <ul className="mt-3 flex flex-wrap justify-center gap-2">
                {insurance.map((name) => (
                  <li
                    key={name}
                    className="rounded-full border border-slate-200 bg-white px-3 py-1 text-sm text-slate-700"
                  >
                    {name}
                  </li>
                ))}
              </ul>
            </>
          )}
          {paymentNote && <p className="mt-4 text-sm text-slate-600">{paymentNote}</p>}
        </div>
      )}

      {ctaText && ctaUrl && (
        <div className="mt-10 text-center">
          <Link
            href={ctaUrl}
            className="inline-flex min-h-[44px] items-center justify-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-8 py-3 text-base font-semibold text-white hover:opacity-90"
          >
            {ctaText}
          </Link>
        </div>
      )}
    </section>
  );
}
