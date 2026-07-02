import { fetchFaq } from "@/lib/api";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";

export async function FaqPage({ page }: PageTypeProps) {
  const layout = page.layout || "accordion";
  const items = await fetchFaq(page.id);

  return (
    <article className="mx-auto max-w-6xl px-4 py-8">
      <BlockRenderer blocks={page.blocks} />
      <h1 className="mb-6 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
        {page.title}
      </h1>
      {layout === "two-col" ? (
        <div className="grid gap-4 md:grid-cols-2">
          {items.map((item) => (
            <FaqItem key={item.id} question={item.question} answer={item.answer} />
          ))}
        </div>
      ) : layout === "search-first" ? (
        <div className="flex flex-col gap-6">
          <input
            type="search"
            placeholder="Search FAQs..."
            className="min-h-[44px] w-full rounded-[var(--border-radius)] border border-slate-300 px-3"
            readOnly
            aria-label="Search FAQs"
          />
          <div className="flex flex-col gap-3">
            {items.map((item) => (
              <FaqItem key={item.id} question={item.question} answer={item.answer} />
            ))}
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {items.map((item) => (
            <details
              key={item.id}
              className="rounded-[var(--border-radius)] border border-slate-200 p-4"
            >
              <summary className="cursor-pointer text-lg font-medium">{item.question}</summary>
              <div
                className="mt-3 text-slate-600"
                dangerouslySetInnerHTML={{ __html: item.answer }}
              />
            </details>
          ))}
        </div>
      )}
    </article>
  );
}

function FaqItem({ question, answer }: { question: string; answer: string }) {
  return (
    <div className="rounded-[var(--border-radius)] border border-slate-200 p-4">
      <h3 className="font-semibold">{question}</h3>
      <div className="mt-2 text-slate-600" dangerouslySetInnerHTML={{ __html: answer }} />
    </div>
  );
}
