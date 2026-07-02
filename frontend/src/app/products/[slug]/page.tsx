import Image from "next/image";
import { notFound } from "next/navigation";
import { fetchProduct } from "@/lib/api";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import {
  STOCK_STATUS_BADGE_COLORS,
  STOCK_STATUS_LABELS,
  STORE_SECTION_LABELS,
  formatPrice,
} from "@/types/product";
import type { Metadata } from "next";

interface Props {
  params: { slug: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const product = await fetchProduct(params.slug).catch(() => null);
  if (!product) return {};
  return {
    title: product.metaTitle || product.name,
    description: product.metaDescription || undefined,
    openGraph: product.ogImageUrl ? { images: [product.ogImageUrl] } : undefined,
    alternates: product.canonicalUrl ? { canonical: product.canonicalUrl } : undefined,
  };
}

export default async function ProductDetailPage({ params }: Props) {
  const product = await fetchProduct(params.slug).catch(() => null);
  if (!product) notFound();

  return (
    <main className="mx-auto max-w-3xl px-4 py-8 md:py-12">
      <AnalyticsTracker
        eventType="product_view"
        metadata={{ slug: params.slug, productName: product.name }}
      />
      <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6 md:p-10">
        <div className="flex flex-col gap-6 md:flex-row md:items-start">
          {product.photoUrl && (
            <Image
              src={product.photoUrl}
              alt={product.name}
              width={200}
              height={200}
              unoptimized
              className="h-48 w-48 rounded-[var(--border-radius)] object-cover"
            />
          )}
          <div className="flex-1">
            <h1 className="text-3xl font-bold">{product.name}</h1>
            {product.brand && (
              <p className="text-lg text-slate-600">{product.brand}</p>
            )}
            <p className="mt-2 text-xl font-semibold text-slate-900">
              {formatPrice(product.price, product.unit)}
            </p>
            <div className="mt-3 flex flex-wrap gap-2">
              <span
                className={`rounded-full px-3 py-1 text-sm font-medium ${STOCK_STATUS_BADGE_COLORS[product.stockStatus]}`}
              >
                {STOCK_STATUS_LABELS[product.stockStatus]}
              </span>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-800">
                {STORE_SECTION_LABELS[product.storeSection]}
              </span>
            </div>
          </div>
        </div>

        {product.description && (
          <div className="prose mt-8 max-w-none">
            <h2 className="text-xl font-semibold">About this item</h2>
            <p className="whitespace-pre-line">{product.description}</p>
          </div>
        )}

        {product.allergyTypes.length > 0 && (
          <div className="mt-8">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Free of</h3>
            <div className="flex flex-wrap gap-2">
              {product.allergyTypes.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-amber-50 px-3 py-1 text-sm text-amber-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}

        {product.dietTypes.length > 0 && (
          <div className="mt-4">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Diet types</h3>
            <div className="flex flex-wrap gap-2">
              {product.dietTypes.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-blue-50 px-3 py-1 text-sm text-blue-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}

        {product.categories.length > 0 && (
          <div className="mt-4">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Categories</h3>
            <div className="flex flex-wrap gap-2">
              {product.categories.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-purple-50 px-3 py-1 text-sm text-purple-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
