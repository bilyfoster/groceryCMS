"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchTaxonomies, submitMatch } from "@/lib/api";
import { trackEvent } from "@/lib/analytics";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import type { TaxonomyTerm } from "@/types/taxonomy";
import type { IntakeRequest, MatchResult } from "@/types/match";
import {
  AVAILABILITY_BADGE_COLORS,
  AVAILABILITY_LABELS,
  SERVICE_DELIVERY_LABELS,
} from "@/types/therapist";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const STEPS = ["safety", "concerns", "preferences", "contact", "results"] as const;
type Step = (typeof STEPS)[number];

export default function MatchPage() {
  const [step, setStep] = useState<Step>("safety");
  const [inCrisis, setInCrisis] = useState(false);
  const [focusAreas, setFocusAreas] = useState<TaxonomyTerm[]>([]);
  const [modalities, setModalities] = useState<TaxonomyTerm[]>([]);
  const [demographics, setDemographics] = useState<TaxonomyTerm[]>([]);
  const [loadingTerms, setLoadingTerms] = useState(true);

  const [intake, setIntake] = useState<IntakeRequest>({});
  const [results, setResults] = useState<MatchResult[] | null>(null);
  const [loadingResults, setLoadingResults] = useState(false);

  useEffect(() => {
    trackEvent("match_start");
    Promise.all([
      fetchTaxonomies("FOCUS_AREA"),
      fetchTaxonomies("MODALITY"),
      fetchTaxonomies("DEMOGRAPHIC"),
    ])
      .then(([fa, mo, de]) => {
        setFocusAreas(fa);
        setModalities(mo);
        setDemographics(de);
      })
      .catch(() => alert("Failed to load form options"))
      .finally(() => setLoadingTerms(false));
  }, []);

  const toggleTerm = (
    field: "areasOfConcern" | "preferredModalities",
    termId: string,
    checked: boolean,
  ) => {
    setIntake((prev) => {
      const current = new Set(prev[field] ?? []);
      if (checked) current.add(termId);
      else current.delete(termId);
      return { ...prev, [field]: Array.from(current) };
    });
  };

  const handleSubmit = async () => {
    setLoadingResults(true);
    try {
      const response = await submitMatch(intake);
      setResults(response.matches);
      trackEvent("match_complete", {
        matchCount: response.matches.length,
        focusAreaCount: intake.areasOfConcern?.length ?? 0,
      });
      setStep("results");
    } catch {
      alert("Failed to get matches. Please try again.");
    } finally {
      setLoadingResults(false);
    }
  };

  const selectedFocusAreaLabels = focusAreas
    .filter((t) => intake.areasOfConcern?.includes(t.id))
    .map((t) => t.label);

  if (loadingTerms) {
    return (
      <main className="mx-auto max-w-2xl px-4 py-12 text-center">
        <p className="text-slate-600">Loading…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl px-4 py-8 md:py-12">
      <AnalyticsTracker eventType="match_start" />
      <h1 className="mb-2 text-3xl font-bold">Find your therapist</h1>
      <p className="mb-8 text-slate-600">
        Answer a few questions and we’ll suggest therapists who may be a good fit.
      </p>

      {step !== "results" && (
        <nav aria-label="Progress" className="mb-8">
          <ol className="flex items-center justify-between text-sm font-medium">
            {[
              { key: "concerns", label: "Concerns" },
              { key: "preferences", label: "Preferences" },
              { key: "contact", label: "Contact" },
            ].map((s, index) => {
              const active = step === (s.key as Step);
              const completed = STEPS.indexOf(step) > STEPS.indexOf(s.key as Step);
              return (
                <li key={s.key} className="flex flex-1 items-center">
                  <span
                    className={`flex h-8 w-8 items-center justify-center rounded-full ${
                      active || completed
                        ? "bg-[var(--color-primary)] text-white"
                        : "bg-slate-100 text-slate-500"
                    }`}
                  >
                    {completed ? "✓" : index + 1}
                  </span>
                  <span
                    className={`ml-2 hidden sm:inline ${
                      active || completed ? "text-slate-900" : "text-slate-500"
                    }`}
                  >
                    {s.label}
                  </span>
                  {index < 2 && <span className="mx-2 flex-1 border-t border-slate-200" />}
                </li>
              );
            })}
          </ol>
        </nav>
      )}

      {step === "safety" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-2 text-xl font-semibold">Before we begin</h2>
          <p className="mb-4 text-sm text-slate-600">
            This tool suggests therapists based on your preferences. It isn’t for emergencies and
            doesn’t provide crisis care.
          </p>
          <div
            className="mb-6 rounded-[var(--border-radius)] border border-red-200 bg-red-50 p-4"
            role="note"
          >
            <p className="text-sm font-semibold text-red-900">
              If you’re in crisis or thinking about harming yourself, help is available right now:
            </p>
            <ul className="mt-2 space-y-1 text-sm text-red-900">
              <li>
                <strong>Call or text 988</strong> — Suicide &amp; Crisis Lifeline (24/7)
              </li>
              <li>
                <strong>Text HOME to 741741</strong> — Crisis Text Line
              </li>
              <li>
                <strong>Call 911</strong> or go to your nearest emergency room if you’re in immediate
                danger
              </li>
            </ul>
          </div>
          {!inCrisis ? (
            <>
              <p className="mb-4 text-sm font-medium">
                Are you currently in immediate danger or thinking about harming yourself?
              </p>
              <div className="flex flex-col gap-3 sm:flex-row">
                <Button variant="outline" onClick={() => setInCrisis(true)}>
                  Yes — I need help now
                </Button>
                <Button onClick={() => setStep("concerns")}>No — continue to matching</Button>
              </div>
            </>
          ) : (
            <div>
              <p className="mb-4 text-sm text-slate-700">
                Please reach out to one of the resources above — you deserve immediate support, and
                this matching tool isn’t the right place for an emergency. If you’re safe and would
                still like to continue, you can proceed.
              </p>
              <div className="flex flex-col gap-3 sm:flex-row">
                <a
                  href="tel:988"
                  className="inline-flex min-h-[44px] items-center justify-center rounded-[var(--border-radius)] bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
                >
                  Call or text 988 now
                </a>
                <Button variant="outline" onClick={() => setStep("concerns")}>
                  I’m safe — continue
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {step === "concerns" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">What brings you in?</h2>
          <p className="mb-4 text-sm text-slate-600">Select all that apply.</p>
          <div className="flex flex-wrap gap-3">
            {focusAreas.map((term) => (
              <label
                key={term.id}
                className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
              >
                <input
                  type="checkbox"
                  checked={intake.areasOfConcern?.includes(term.id) ?? false}
                  onChange={(e) => toggleTerm("areasOfConcern", term.id, e.target.checked)}
                />
                <span className="text-sm">{term.label}</span>
              </label>
            ))}
          </div>
          <div className="mt-6 flex justify-end">
            <Button
              onClick={() => setStep("preferences")}
              disabled={!intake.areasOfConcern?.length}
            >
              Continue
            </Button>
          </div>
        </div>
      )}

      {step === "preferences" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">Tell us your preferences</h2>

          <div className="mb-6">
            <Label htmlFor="demographic">I am seeking support for</Label>
            <select
              id="demographic"
              value={intake.clientDemographic || ""}
              onChange={(e) =>
                setIntake((prev) => ({
                  ...prev,
                  clientDemographic: e.target.value || undefined,
                }))
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">Select…</option>
              {demographics.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-6">
            <Label htmlFor="delivery">Preferred session type</Label>
            <select
              id="delivery"
              value={intake.preferredDelivery || ""}
              onChange={(e) =>
                setIntake((prev) => ({
                  ...prev,
                  preferredDelivery: (e.target.value || undefined) as
                    | IntakeRequest["preferredDelivery"]
                    | undefined,
                }))
              }
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">No preference</option>
              {Object.entries(SERVICE_DELIVERY_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <fieldset className="mb-6">
            <legend className="mb-2 text-sm font-medium">Preferred modalities</legend>
            <div className="flex flex-wrap gap-3">
              {modalities.map((term) => (
                <label
                  key={term.id}
                  className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={intake.preferredModalities?.includes(term.id) ?? false}
                    onChange={(e) => toggleTerm("preferredModalities", term.id, e.target.checked)}
                  />
                  <span className="text-sm">{term.label}</span>
                </label>
              ))}
            </div>
          </fieldset>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label htmlFor="gender">Therapist gender preference</Label>
              <Input
                id="gender"
                value={intake.therapistGenderPreference || ""}
                onChange={(e) =>
                  setIntake((prev) => ({ ...prev, therapistGenderPreference: e.target.value }))
                }
                placeholder="Optional"
              />
            </div>
            <div>
              <Label htmlFor="identity">Therapist identity preference</Label>
              <Input
                id="identity"
                value={intake.therapistIdentityPreference || ""}
                onChange={(e) =>
                  setIntake((prev) => ({ ...prev, therapistIdentityPreference: e.target.value }))
                }
                placeholder="Optional"
              />
            </div>
          </div>

          <div className="mt-6 flex justify-between">
            <Button variant="outline" onClick={() => setStep("concerns")}>
              Back
            </Button>
            <Button onClick={() => setStep("contact")}>Continue</Button>
          </div>
        </div>
      )}

      {step === "contact" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">Stay in the loop</h2>
          <p className="mb-4 text-sm text-slate-600">
            Your email is optional and only used to follow up. No clinical information is stored.
          </p>
          <div className="mb-6">
            <Label htmlFor="email">Email address</Label>
            <Input
              id="email"
              type="email"
              value={intake.contactEmail || ""}
              onChange={(e) =>
                setIntake((prev) => ({ ...prev, contactEmail: e.target.value }))
              }
              placeholder="you@example.com"
            />
          </div>

          {selectedFocusAreaLabels.length > 0 && (
            <div className="mb-6 text-sm text-slate-600">
              You selected: {selectedFocusAreaLabels.join(", ")}
            </div>
          )}

          <div className="mt-6 flex justify-between">
            <Button variant="outline" onClick={() => setStep("preferences")}>
              Back
            </Button>
            <Button onClick={handleSubmit} disabled={loadingResults}>
              {loadingResults ? "Matching…" : "See my matches"}
            </Button>
          </div>
        </div>
      )}

      {step === "results" && results && (
        <div>
          <div className="mb-6 flex items-center justify-between">
            <h2 className="text-2xl font-bold">Your matches</h2>
            <Button variant="outline" onClick={() => { setStep("concerns"); setIntake({}); setResults(null); }}>
              Start over
            </Button>
          </div>

          {results.length === 0 ? (
            <p className="text-slate-600">
              We couldn’t find a match based on those preferences. Try widening your filters or
              browse the{" "}
              <Link href="/therapists" className="text-[var(--color-primary)] underline">
                therapist directory
              </Link>
              .
            </p>
          ) : (
            <ul className="flex flex-col gap-4">
              {results.map((match) => (
                <li
                  key={match.therapistId}
                  className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6"
                >
                  <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <h3 className="text-xl font-semibold">
                        {match.rank}. {match.firstName} {match.lastName}
                      </h3>
                      {match.credentials && (
                        <p className="text-slate-600">{match.credentials}</p>
                      )}
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
      )}
    </main>
  );
}
