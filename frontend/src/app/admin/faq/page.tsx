import { fetchNavPages } from "@/lib/api";

export default async function AdminFaqPage() {
  const pages = await fetchNavPages().catch(() => []);
  const faqPages = pages.filter((p) => p.pageType === "faq");

  return (
    <div className="p-4 md:p-8">
      <h1 className="mb-4 text-2xl font-bold">FAQ</h1>
      <p className="text-sm text-slate-600">
        FAQ items are stored per FAQ page. Use the API or future editor to manage questions.
      </p>
      <ul className="mt-4 flex flex-col gap-2">
        {faqPages.map((p) => (
          <li key={p.id} className="rounded border border-slate-200 p-3">
            {p.title}
          </li>
        ))}
      </ul>
    </div>
  );
}
