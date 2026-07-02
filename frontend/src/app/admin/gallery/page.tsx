import { fetchNavPages } from "@/lib/api";

export default async function AdminGalleryPage() {
  const pages = await fetchNavPages().catch(() => []);
  const galleryPages = pages.filter((p) => p.pageType === "gallery");

  return (
    <div className="p-4 md:p-8">
      <h1 className="mb-4 text-2xl font-bold">Gallery</h1>
      <p className="text-sm text-slate-600">
        Upload images in Media, then attach them to gallery pages via the API.
      </p>
      <ul className="mt-4 flex flex-col gap-2">
        {galleryPages.map((p) => (
          <li key={p.id} className="rounded border border-slate-200 p-3">
            {p.title} — <code>pageId: {p.id}</code>
          </li>
        ))}
      </ul>
    </div>
  );
}
