"use client";

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { fetchProducts, fetchTaxonomies } from "@/lib/api";
import type { TaxonomyTerm, TaxonomyType } from "@/types/taxonomy";
import type { ProductFilters, ProductSummary } from "@/types/product";
import {
  STOCK_STATUS_BADGE_COLORS,
  STOCK_STATUS_LABELS,
  STORE_SECTION_LABELS,
  formatPrice,
} from "@/types/product";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const SORT_OPTIONS = [
  { value: "recommended", label: "Recommended" },
  { value: "name-asc", label: "Name: A → Z" },
  { value: "name-desc", label: "Name: Z → A" },
];

const FILTER_LABELS: Record<keyof ProductFilters, string> = {
  allergyType: "Free of",
  dietType: "Diet",
  category: "Category",
  storeSection: "Store section",
  stockStatus: "Stock status",
  q: "Search",
};

export default function ProductsDirectoryPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [sort, setSort] = useState("recommended");
  const [filters, setFilters] = useState<ProductFilters>({});

  useEffect(() => {
    const types: TaxonomyType[] = ["ALLERGY_TYPE", "DIET_TYPE", "PRODUCT_CATEGORY"];
    Promise.all(types.map((type) => fetchTaxonomies(type)))
      .then((results) => setTerms(results.flat()))
      .catch(() => alert("Failed to load filters"));
  }, []);

  useEffect(() => {
    const initial: ProductFilters = {
      allergyType: searchParams.get("allergyType") || undefined,
      dietType: searchParams.get("dietType") || undefined,
      category: searchParams.get("category") || undefined,
      storeSection: (searchParams.get("storeSection") as ProductFilters["storeSection"]) || undefined,
      stockStatus: (searchParams.get("stockStatus") as ProductFilters["stockStatus"]) || undefined,
      q: searchParams.get("q") || undefined,
    };
    setFilters(initial);
    setSort(searchParams.get("sort") || "recommended");
    setPage(parseInt(searchParams.get("page") || "0", 10));
  }, [searchParams]);

  useEffect(() => {
    setLoading(true);
    fetchProducts(filters, page, 12)
      .then((response) => {
        let items = response.items;
        if (sort === "name-asc") {
          items = [...items].sort((a, b) => a.name.localeCompare(b.name));
        } else if (sort === "name-desc") {
          items = [...items].sort((a, b) => b.name.localeCompare(a.name));
        }
        setProducts(items);
        setTotalPages(response.totalPages);
      })
      .catch(() => alert("Failed to load products"))
      .finally(() => setLoading(false));
  }, [filters, page, sort]);

  const termsByType = useMemo(() => {
    const map: Record<TaxonomyType, TaxonomyTerm[]> = {
      ALLERGY_TYPE: [],
      DIET_TYPE: [],
      PRODUCT_CATEGORY: [],
    };
    terms.forEach((term) => map[term.type].push(term));
    Object.values(map).forEach((list) => list.sort((a, b) => a.sortOrder - b.sortOrder));
    return map;
  }, [terms]);

  const updateFilter = <K extends keyof ProductFilters>(key: K, value: ProductFilters[K]) => {
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
    router.push(`/products?${params.toString()}`);
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
              placeholder="Product or brand"
            />
          </div>

          <div className="mb-4">
            <Label htmlFor="allergyType">{FILTER_LABELS.allergyType}</Label>
            <select
              id="allergyType"
              value={filters.allergyType || ""}
              onChange={(e) => updateFilter("allergyType", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.ALLERGY_TYPE.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="dietType">{FILTER_LABELS.dietType}</Label>
            <select
              id="dietType"
              value={filters.dietType || ""}
              onChange={(e) => updateFilter("dietType", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.DIET_TYPE.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="category">{FILTER_LABELS.category}</Label>
            <select
              id="category"
              value={filters.category || ""}
              onChange={(e) => updateFilter("category", e.target.value || undefined)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {termsByType.PRODUCT_CATEGORY.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="storeSection">{FILTER_LABELS.storeSection}</Label>
            <select
              id="storeSection"
              value={filters.storeSection || ""}
              onChange={(e) =>
                updateFilter("storeSection", (e.target.value || undefined) as ProductFilters["storeSection"])
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {Object.entries(STORE_SECTION_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-4">
            <Label htmlFor="stockStatus">{FILTER_LABELS.stockStatus}</Label>
            <select
              id="stockStatus"
              value={filters.stockStatus || ""}
              onChange={(e) =>
                updateFilter("stockStatus", (e.target.value || undefined) as ProductFilters["stockStatus"])
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Any</option>
              {Object.entries(STOCK_STATUS_LABELS).map(([value, label]) => (
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
          <h1 className="text-2xl font-bold">Shop allergy-friendly foods</h1>
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
        ) : products.length === 0 ? (
          <p className="text-slate-600">No products match your filters.</p>
        ) : (
          <>
            <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
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

function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <li className="flex flex-col rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
      <div className="flex items-start gap-4">
        {product.photoUrl ? (
          <Image
            src={product.photoUrl}
            alt={product.name}
            width={80}
            height={80}
            unoptimized
            className="h-20 w-20 rounded-[var(--border-radius)] object-cover"
          />
        ) : (
          <div className="flex h-20 w-20 items-center justify-center rounded-[var(--border-radius)] bg-slate-100 text-2xl font-bold text-slate-400">
            {product.name[0]}
          </div>
        )}
        <div className="flex-1">
          <Link
            href={`/products/${product.slug}`}
            className="text-lg font-semibold hover:underline"
          >
            {product.name}
          </Link>
          {product.brand && (
            <p className="text-sm text-slate-600">{product.brand}</p>
          )}
          <p className="text-sm font-medium text-slate-800">
            {formatPrice(product.price, product.unit)}
          </p>
          <span
            className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${STOCK_STATUS_BADGE_COLORS[product.stockStatus]}`}
          >
            {STOCK_STATUS_LABELS[product.stockStatus]}
          </span>
        </div>
      </div>

      {(product.allergyTypes.length > 0 || product.dietTypes.length > 0) && (
        <div className="mt-4 flex flex-wrap gap-2">
          {product.allergyTypes.slice(0, 3).map((term) => (
            <span
              key={term.id}
              className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-800"
            >
              {term.label}
            </span>
          ))}
          {product.dietTypes.slice(0, 2).map((term) => (
            <span
              key={term.id}
              className="rounded-full bg-blue-50 px-2 py-0.5 text-xs text-blue-800"
            >
              {term.label}
            </span>
          ))}
          {product.allergyTypes.length + product.dietTypes.length > 5 && (
            <span className="text-xs text-slate-500">
              +{product.allergyTypes.length + product.dietTypes.length - 5} more
            </span>
          )}
        </div>
      )}

      <div className="mt-auto flex gap-2 pt-4">
        <Link
          href={`/products/${product.slug}`}
          className="inline-flex min-h-[36px] items-center rounded-[var(--border-radius)] border border-slate-200 px-3 py-1 text-sm font-medium hover:bg-slate-50"
        >
          View product
        </Link>
      </div>
    </li>
  );
}
