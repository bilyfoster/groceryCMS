"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  AVAILABILITY_BADGE_COLORS,
  AVAILABILITY_LABELS,
} from "@/types/therapist";
import type { MatchResult } from "@/types/match";

interface TherapistMatchListProps {
  matches: MatchResult[];
  onReset: () => void;
}

export function TherapistMatchList({ matches, onReset }: TherapistMatchListProps) {
  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Your matches</h2>
        <Button variant="outline" onClick={onReset}>
          Start over
        </Button>
      </div>

      {matches.length === 0 ? (
        <p className="text-slate-600">
          We couldn’t find a match based on those preferences. Try widening your answers or browse the{" "}
          <Link href="/therapists" className="text-[var(--color-primary)] underline">
            therapist directory
          </Link>
          .
        </p>
      ) : (
        <ul className="flex flex-col gap-4">
          {matches.map((match) => (
            <li
              key={match.therapistId}
              className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6"
            >
              <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div>
                  <h3 className="text-xl font-semibold">
                    {match.rank}. {match.firstName} {match.lastName}
                  </h3>
                  {match.credentials && <p className="text-slate-600">{match.credentials}</p>}
                  <span
                    className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${AVAILABILITY_BADGE_COLORS[match.availabilityStatus]}`}
                  >
                    {AVAILABILITY_LABELS[match.availabilityStatus]}
                  </span>
                </div>
                <Link
                  href={`/therapists/${match.slug}`}
                  className="inline-flex min-h-[36px] items-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-white hover:opacity-90"
                >
                  View profile
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
