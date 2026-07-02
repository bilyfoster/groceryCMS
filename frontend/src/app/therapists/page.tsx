"use client";

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { fetchTaxonomies, fetchTherapists } from "@/lib/api";
import type { TaxonomyTerm, TaxonomyType } from "@/types/taxonomy";
import type { TherapistFilters, TherapistSummary } from "@/types/therapist";
import {
  AVAILABILITY_BADGE_COLORS,
  AVAILABILITY_LABELS,
  SERVICE_DELIVERY_LABELS,
} from "@/types/therapist";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const SORT_OPTIONS = [
  { value: "recommended", label: "Recommended" },
  { value: "name-asc", label: "Name: A → Z" },
  { value: "name-desc", label: "Name: Z → A" },
];

const FILTER_LABELS: Record<keyof TherapistFilters, string> = {
  focusArea: "Focus area",
  modality: "Modality",
  demographic: "Demographic",
  delivery: "Service delivery",
  availability: "Availability",
  q: "Search",
};

export default function TherapistsDirectoryPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [therapists, setTherapists] = useState<TherapistSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [sort, setSort] = useState("recommended");
  const [filters, setFilters] = useState<TherapistFilters>({});

  useEffect(() => {
    const types: TaxonomyType[] = ["FOCUS_AREA", "MODALITY", "DEMOGRAPHIC"];
    Promise.all(types.map((type) => fetchTaxonomies(type)))
      .then((results) => setTerms(results.flat()))
      .catch(() => alert("Failed to load filters"));
  }, []);

  useEffect(() => {
    const initial: TherapistFilters = {
      focusArea: searchParams.get("focusArea") || undefined,
      modality: searchParams.get("modality") || undefined,
      demographic: searchParams.get("demographic") || undefined,
      delivery: (searchParams.get("delivery") as TherapistFilters["delivery"]) || undefined,
      availability: (searchParams.get("availability") as TherapistFilters["availability"]) || undefined,
      q: searchParams.get("q") || undefined,
    };
    setFilters(initial);
    setSort(searchParams.get("sort") || "recommended");
    setPage(parseInt(searchParams.get("page") || "0", 10));
  }, [searchParams]);

  useEffect(() => {
    setLoading(true);
    fetchTherapists(filters, page, 12)
      .then((response) => {
        let items = response.items;
        if (sort === "name-asc") {
          items = [...items].sort((a, b) =>
            `${a.lastName} ${a.firstName}`.localeCompare(`${b.lastName} ${b.firstName}`),
          );
        } else if (sort === "name-desc") {
          items = [...items].sort((a, b) =>
            `${b.lastName} ${b.firstName}`.localeCompare(`${a.lastName} ${a.firstName}`),
          );
        }
        setTherapists(items);
        setTotalPages(response.totalPages);
      })
      .catch(() => alert("Failed to load therapists"))
      .finally(() => setLoading(false));
  }, [filters, page, sort]);

  const termsByType = useMemo(() => {
    const map: Record<TaxonomyType, TaxonomyTerm[]> = {
      FOCUS_AREA: [],
      MODALITY: [],
      DEMOGRAPHIC: [],
    };
    terms.forEach((term) => map[term.type].push(term));
    Object.values(map).forEach((list) => list.sort((a, b) => a.sortOrder - b.sortOrder));
    return map;
  }, [terms]);

  const updateFilter = <K extends keyof TherapistFilters>(key: K, value: TherapistFilters[K]) => {
    setPage(0);
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const applyFilters = () => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });
    if (sort !== "recommended") params.set("sort", sort);
    if (page > 0) params.set("page", String(page));
    router.push(`/therapists?${params.toString()}`);
  };

  useEffect(() => {
    applyFilters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, sort, page]);

  const clearFilters = () => {
    setFilters({});
    setSort("recommended");
    setPage(0);
  };

  return (
    <main className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-8 md:flex-row md:py-12">
      <aside className="w-full shrink-0 md:w-64">
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
          <h2 className="mb-4 text-lg font-semibold">Filters</h2>

          <div className="mb-4">
            <Label htmlFor="q">Search</Label>
            <Input
              id="q"
              value={filters.q || ""}
              onChange={(e) => updateFilter("q", e.target.value)}
              placeholder="Name or credentials"
            />
          </div>

          <div className="mb-4">
            <Label htmlFor="focusArea">{FILTER_LABELS.focusArea}</Label>
            <select
              id="focusArea"
              value={filters.focusArea || ""}
              onChange={(e) => updateFilter("focusArea", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.FOCUS_AREA.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="modality">{FILTER_LABELS.modality}</Label>
            <select
              id="modality"
              value={filters.modality || ""}
              onChange={(e) => updateFilter("modality", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.MODALITY.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="demographic">{FILTER_LABELS.demographic}</Label>
            <select
              id="demographic"
              value={filters.demographic || ""}
              onChange={(e) => updateFilter("demographic", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.DEMOGRAPHIC.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="delivery">{FILTER_LABELS.delivery}</Label>
            <select
              id="delivery"
              value={filters.delivery || ""}
              onChange={(e) =>
                updateFilter("delivery", (e.target.value || undefined) as TherapistFilters["delivery"])
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {Object.entries(SERVICE_DELIVERY_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="availability">{FILTER_LABELS.availability}</Label>
            <select
              id="availability"
              value={filters.availability || ""}
              onChange={(e) =>
                updateFilter(
                  "availability",
                  (e.target.value || undefined) as TherapistFilters["availability"],
                )
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {Object.entries(AVAILABILITY_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <Button variant="outline" className="w-full" onClick={clearFilters}>
            Clear filters
          </Button>
        </div>
      </aside>

      <section className="flex-1">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-2xl font-bold">Our therapists</h1>
          <div className="flex items-center gap-2">
            <Label htmlFor="sort" className="sr-only">
              Sort
            </Label>
            <select
              id="sort"
              value={sort}
              onChange={(e) => setSort(e.target.value)}
              className="rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <p className="text-slate-600">Loading…</p>
        ) : therapists.length === 0 ? (
          <p className="text-slate-600">No therapists match your filters.</p>
        ) : (
          <>
            <ul className="grid gap-4 sm:grid-cols-2">
              {therapists.map((therapist) => (
                <TherapistCard key={therapist.id} therapist={therapist} />
              ))}
            </ul>

            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Previous
                </Button>
                <span className="text-sm text-slate-600">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
      </section>
    </main>
  );
}

function TherapistCard({ therapist }: { therapist: TherapistSummary }) {
  const fullName = `${therapist.firstName} ${therapist.lastName}`;
  return (
    <li className="flex flex-col rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
      <div className="flex items-start gap-4">
        {therapist.photoUrl ? (
          <Image
            src={therapist.photoUrl}
            alt={`Portrait of ${fullName}`}
            width={80}
            height={80}
            unoptimized
            className="h-20 w-20 rounded-[var(--border-radius)] object-cover"
          />
        ) : (
          <div className="flex h-20 w-20 items-center justify-center rounded-[var(--border-radius)] bg-slate-100 text-2xl font-bold text-slate-400">
            {therapist.firstName[0]}
            {therapist.lastName[0]}
          </div>
        )}
        <div className="flex-1">
          <Link
            href={`/therapists/${therapist.slug}`}
            className="text-lg font-semibold hover:underline"
          >
            {fullName}
          </Link>
          {therapist.credentials && (
            <p className="text-sm text-slate-600">{therapist.credentials}</p>
          )}
          <span
            className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${AVAILABILITY_BADGE_COLORS[therapist.availabilityStatus]}`}
          >
            {AVAILABILITY_LABELS[therapist.availabilityStatus]}
          </span>
        </div>
      </div>

      {therapist.focusAreas.length > 0 && (
        <div className="mt-4 flex flex-wrap gap-2">
          {therapist.focusAreas.slice(0, 4).map((term) => (
            <span
              key={term.id}
              className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-800"
            >
              {term.label}
            </span>
          ))}
          {therapist.focusAreas.length > 4 && (
            <span className="text-xs text-slate-500">
              +{therapist.focusAreas.length - 4} more
            </span>
          )}
        </div>
      )}

      <div className="mt-auto flex gap-2 pt-4">
        <Link
          href={`/therapists/${therapist.slug}`}
          className="inline-flex min-h-[36px] items-center rounded-[var(--border-radius)] border border-slate-200 px-3 py-1 text-sm font-medium hover:bg-slate-50"
        >
          View profile
        </Link>
      </div>
    </li>
  );
}
