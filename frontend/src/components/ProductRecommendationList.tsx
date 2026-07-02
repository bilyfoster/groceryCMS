"use client";

import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  STOCK_STATUS_BADGE_COLORS,
  STOCK_STATUS_LABELS,
  formatPrice,
} from "@/types/product";
import type { MatchResult } from "@/types/match";

interface ProductRecommendationListProps {
  matches: MatchResult[];
  awarenessNotes?: string[];
  onReset: () => void;
}

export function ProductRecommendationList({
  matches,
  awarenessNotes,
  onReset,
}: ProductRecommendationListProps) {
  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Recommended for you</h2>
        <Button variant="outline" onClick={onReset}>
          Start over
        </Button>
      </div>

      {awarenessNotes && awarenessNotes.length > 0 && (
        <div
          className="mb-6 rounded-[var(--border-radius)] border border-amber-200 bg-amber-50 p-4"
          role="note"
        >
          <p className="text-sm font-semibold text-amber-900">Allergy awareness</p>
          <ul className="mt-2 list-inside list-disc space-y-1 text-sm text-amber-900">
            {awarenessNotes.map((note, idx) => (
              <li key={idx}>{note}</li>
            ))}
          </ul>
        </div>
      )}

      {matches.length === 0 ? (
        <p className="text-slate-600">
          We couldn’t find a product based on those preferences. Try widening your answers or browse the{" "}
          <Link href="/products" className="text-[var(--color-primary)] underline">
            product catalog
          </Link>
          .
        </p>
      ) : (
        <ul className="flex flex-col gap-4">
          {matches.map((match) => (
            <li
              key={match.productId}
              className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6"
            >
              <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div className="flex items-start gap-4">
                  {match.photoUrl ? (
                    <Image
                      src={match.photoUrl}
                      alt={match.name}
                      width={80}
                      height={80}
                      unoptimized
                      className="h-20 w-20 rounded-[var(--border-radius)] object-cover"
                    />
                  ) : (
                    <div className="flex h-20 w-20 items-center justify-center rounded-[var(--border-radius)] bg-slate-100 text-2xl font-bold text-slate-400">
                      {match.name[0]}
                    </div>
                  )}
                  <div>
                    <h3 className="text-xl font-semibold">
                      {match.rank}. {match.name}
                    </h3>
                    {match.brand && <p className="text-slate-600">{match.brand}</p>}
                    <p className="font-medium text-slate-800">
                      {formatPrice(match.price, match.unit)}
                    </p>
                    <span
                      className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${STOCK_STATUS_BADGE_COLORS[match.stockStatus]}`}
                    >
                      {STOCK_STATUS_LABELS[match.stockStatus]}
                    </span>
                  </div>
                </div>
                <Link
                  href={`/products/${match.slug}`}
                  className="inline-flex min-h-[36px] items-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-white hover:opacity-90"
                >
                  View product
                </Link>
              </div>

              {match.explanations.length > 0 && (
                <ul className="mt-4 list-inside list-disc text-sm text-slate-600">
                  {match.explanations.map((explanation, idx) => (
                    <li key={idx}>{explanation}</li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
