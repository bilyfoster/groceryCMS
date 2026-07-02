import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import { fetchProducts, fetchTaxonomies } from "@/lib/api";
import { cn } from "@/lib/utils";
import Link from "next/link";
import Image from "next/image";
import {
  STOCK_STATUS_BADGE_COLORS,
  STOCK_STATUS_LABELS,
  formatPrice,
} from "@/types/product";

export async function ServicePage({ page }: PageTypeProps) {
  const layout = page.layout || "contained";
  const categoryId = page.config?.categoryId as string | undefined;

  const related = categoryId
    ? (await fetchProducts({ category: categoryId }, 0, 6)).items
    : [];
  const category = categoryId
    ? (await fetchTaxonomies("PRODUCT_CATEGORY")).find((t) => t.id === categoryId)
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
              Products {category ? `in ${category.label}` : "for this service"}
            </h2>
            <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {related.map((product) => (
                <li
                  key={product.id}
                  className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-4"
                >
                  {product.photoUrl && (
                    <Image
                      src={product.photoUrl}
                      alt={product.name}
                      width={80}
                      height={80}
                      unoptimized
                      className="mb-3 h-20 w-20 rounded-[var(--border-radius)] object-cover"
                    />
                  )}
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
                  {product.allergyTypes.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-1">
                      {product.allergyTypes.slice(0, 3).map((term) => (
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
                href={`/products?category=${categoryId}`}
                className="text-[var(--color-primary)] hover:underline"
              >
                View all matching products →
              </Link>
            </div>
          </section>
        )}
      </div>
    </article>
  );
}
