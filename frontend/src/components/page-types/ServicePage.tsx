import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import { fetchTaxonomies, fetchTherapists } from "@/lib/api";
import { cn } from "@/lib/utils";
import Link from "next/link";
import {
  AVAILABILITY_BADGE_COLORS,
  AVAILABILITY_LABELS,
} from "@/types/therapist";

export async function ServicePage({ page }: PageTypeProps) {
  const layout = page.layout || "contained";
  const focusAreaId = page.config?.focusAreaId as string | undefined;

  const related = focusAreaId
    ? (await fetchTherapists({ focusArea: focusAreaId }, 0, 6)).items
    : [];
  const focusArea = focusAreaId
    ? (await fetchTaxonomies("FOCUS_AREA")).find((t) => t.id === focusAreaId)
    : null;

  const wrapperClass = cn(
    "mx-auto px-4 py-8",
    layout === "full-width" && "max-w-none px-0",
    layout === "contained" && "max-w-6xl",
    (layout === "sidebar-left" || layout === "sidebar-right") &&
      "max-w-6xl md:grid md:grid-cols-[240px_1fr] md:gap-8",
    layout === "sidebar-right" && "md:grid-cols-[1fr_240px]",
  );

  return (
    <article className={wrapperClass} data-layout={layout}>
      {(layout === "sidebar-left" || layout === "sidebar-right") && (
        <aside className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4 md:mb-0">
          <p className="text-sm text-slate-600">{page.title}</p>
        </aside>
      )}
      <div>
        <BlockRenderer blocks={page.blocks} />

        {related.length > 0 && (
          <section className="mt-12">
            <h2 className="mb-4 text-2xl font-bold">
              Therapists {focusArea ? `specializing in ${focusArea.label}` : "for this service"}
            </h2>
            <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {related.map((therapist) => (
                <li
                  key={therapist.id}
                  className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-4"
                >
                  <Link
                    href={`/therapists/${therapist.slug}`}
                    className="text-lg font-semibold hover:underline"
                  >
                    {therapist.firstName} {therapist.lastName}
                  </Link>
                  {therapist.credentials && (
                    <p className="text-sm text-slate-600">{therapist.credentials}</p>
                  )}
                  <span
                    className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${AVAILABILITY_BADGE_COLORS[therapist.availabilityStatus]}`}
                  >
                    {AVAILABILITY_LABELS[therapist.availabilityStatus]}
                  </span>
                  {therapist.focusAreas.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-1">
                      {therapist.focusAreas.slice(0, 3).map((term) => (
                        <span
                          key={term.id}
                          className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-800"
                        >
                          {term.label}
                        </span>
                      ))}
                    </div>
                  )}
                </li>
              ))}
            </ul>
            <div className="mt-4">
              <Link
                href={`/therapists?focusArea=${focusAreaId}`}
                className="text-[var(--color-primary)] hover:underline"
              >
                View all matching therapists →
              </Link>
            </div>
          </section>
        )}
      </div>
    </article>
  );
}
