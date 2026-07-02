"use client";

import { useEffect, useState } from "react";
import { fetchTaxonomies, submitMatch } from "@/lib/api";
import { trackEvent } from "@/lib/analytics";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import { ProductRecommendationList } from "@/components/ProductRecommendationList";
import type { TaxonomyTerm } from "@/types/taxonomy";
import type { IntakeRequest, MatchResponse } from "@/types/match";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const SYMPTOM_OPTIONS = [
  { value: "bloating", label: "Bloating or gas" },
  { value: "stomach-pain", label: "Stomach pain or cramps" },
  { value: "diarrhea", label: "Diarrhea" },
  { value: "skin-rash", label: "Skin rash or hives" },
  { value: "itching", label: "Itching or tingling in mouth" },
  { value: "headaches", label: "Headaches" },
  { value: "fatigue", label: "Fatigue after eating" },
  { value: "joint-pain", label: "Joint pain or swelling" },
];

const STEPS = ["allergens", "symptoms", "preferences", "results"] as const;
type Step = (typeof STEPS)[number];

export default function MatchPage() {
  const [step, setStep] = useState<Step>("allergens");
  const [allergyTypes, setAllergyTypes] = useState<TaxonomyTerm[]>([]);
  const [dietTypes, setDietTypes] = useState<TaxonomyTerm[]>([]);
  const [categories, setCategories] = useState<TaxonomyTerm[]>([]);
  const [loadingTerms, setLoadingTerms] = useState(true);

  const [intake, setIntake] = useState<IntakeRequest>({});
  const [results, setResults] = useState<MatchResponse | null>(null);
  const [loadingResults, setLoadingResults] = useState(false);

  useEffect(() => {
    trackEvent("match_start");
    Promise.all([
      fetchTaxonomies("ALLERGY_TYPE"),
      fetchTaxonomies("DIET_TYPE"),
      fetchTaxonomies("PRODUCT_CATEGORY"),
    ])
      .then(([at, dt, cat]) => {
        setAllergyTypes(at);
        setDietTypes(dt);
        setCategories(cat);
      })
      .catch(() => alert("Failed to load form options"))
      .finally(() => setLoadingTerms(false));
  }, []);

  const toggleTerm = (
    field: "avoidedAllergies" | "preferredDiets" | "preferredCategories",
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

  const toggleSymptom = (value: string, checked: boolean) => {
    setIntake((prev) => {
      const current = new Set(prev.symptoms ?? []);
      if (checked) current.add(value);
      else current.delete(value);
      return { ...prev, symptoms: Array.from(current) };
    });
  };

  const handleSubmit = async () => {
    setLoadingResults(true);
    try {
      const response = await submitMatch(intake);
      setResults(response);
      trackEvent("match_complete", {
        matchCount: response.matches.length,
        allergyCount: intake.avoidedAllergies?.length ?? 0,
      });
      setStep("results");
    } catch {
      alert("Failed to get recommendations. Please try again.");
    } finally {
      setLoadingResults(false);
    }
  };

  const selectedAllergyLabels = allergyTypes
    .filter((t) => intake.avoidedAllergies?.includes(t.id))
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
      <h1 className="mb-2 text-3xl font-bold">Find safe foods for you</h1>
      <p className="mb-8 text-slate-600">
        Answer a few questions and we’ll suggest gluten-free and allergy-friendly products.
      </p>

      {step !== "results" && (
        <nav aria-label="Progress" className="mb-8">
          <ol className="flex items-center justify-between text-sm font-medium">
            {[
              { key: "allergens", label: "Allergens" },
              { key: "symptoms", label: "Symptoms" },
              { key: "preferences", label: "Preferences" },
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

      {step === "allergens" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">What do you need to avoid?</h2>
          <p className="mb-4 text-sm text-slate-600">Select all that apply. We’ll exclude products that contain these.</p>
          <div className="flex flex-wrap gap-3">
            {allergyTypes.map((term) => (
              <label
                key={term.id}
                className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
              >
                <input
                  type="checkbox"
                  checked={intake.avoidedAllergies?.includes(term.id) ?? false}
                  onChange={(e) => toggleTerm("avoidedAllergies", term.id, e.target.checked)}
                />
                <span className="text-sm">{term.label}</span>
              </label>
            ))}
          </div>
          <div className="mt-6 flex justify-end">
            <Button onClick={() => setStep("symptoms")}>Continue</Button>
          </div>
        </div>
      )}

      {step === "symptoms" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">Have you noticed these symptoms after eating?</h2>
          <p className="mb-4 text-sm text-slate-600">Select any that apply. This helps us surface awareness notes, not a diagnosis.</p>
          <div className="flex flex-wrap gap-3">
            {SYMPTOM_OPTIONS.map((option) => (
              <label
                key={option.value}
                className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
              >
                <input
                  type="checkbox"
                  checked={intake.symptoms?.includes(option.value) ?? false}
                  onChange={(e) => toggleSymptom(option.value, e.target.checked)}
                />
                <span className="text-sm">{option.label}</span>
              </label>
            ))}
          </div>
          <div className="mt-6 flex justify-between">
            <Button variant="outline" onClick={() => setStep("allergens")}>
              Back
            </Button>
            <Button onClick={() => setStep("preferences")}>Continue</Button>
          </div>
        </div>
      )}

      {step === "preferences" && (
        <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-xl font-semibold">Tell us your preferences</h2>

          <fieldset className="mb-6">
            <legend className="mb-2 text-sm font-medium">Diet types</legend>
            <div className="flex flex-wrap gap-3">
              {dietTypes.map((term) => (
                <label
                  key={term.id}
                  className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={intake.preferredDiets?.includes(term.id) ?? false}
                    onChange={(e) => toggleTerm("preferredDiets", term.id, e.target.checked)}
                  />
                  <span className="text-sm">{term.label}</span>
                </label>
              ))}
            </div>
          </fieldset>

          <fieldset className="mb-6">
            <legend className="mb-2 text-sm font-medium">Product categories</legend>
            <div className="flex flex-wrap gap-3">
              {categories.map((term) => (
                <label
                  key={term.id}
                  className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-4 py-2 hover:bg-slate-50"
                >
                  <input
                    type="checkbox"
                    checked={intake.preferredCategories?.includes(term.id) ?? false}
                    onChange={(e) => toggleTerm("preferredCategories", term.id, e.target.checked)}
                  />
                  <span className="text-sm">{term.label}</span>
                </label>
              ))}
            </div>
          </fieldset>

          <div className="mb-6">
            <Label htmlFor="email">Email address (optional)</Label>
            <Input
              id="email"
              type="email"
              value={intake.contactEmail || ""}
              onChange={(e) =>
                setIntake((prev) => ({ ...prev, contactEmail: e.target.value }))
              }
              placeholder="you@example.com"
            />
            <p className="mt-1 text-xs text-slate-500">
              Optional — only used if you want us to follow up with news or availability.
            </p>
          </div>

          {selectedAllergyLabels.length > 0 && (
            <div className="mb-6 text-sm text-slate-600">
              You’re avoiding: {selectedAllergyLabels.join(", ")}
            </div>
          )}

          <div className="mt-6 flex justify-between">
            <Button variant="outline" onClick={() => setStep("symptoms")}>
              Back
            </Button>
            <Button onClick={handleSubmit} disabled={loadingResults}>
              {loadingResults ? "Finding recommendations…" : "See my recommendations"}
            </Button>
          </div>
        </div>
      )}

      {step === "results" && results && (
        <ProductRecommendationList
          matches={results.matches}
          awarenessNotes={results.awarenessNotes}
          onReset={() => {
            setStep("allergens");
            setIntake({});
            setResults(null);
          }}
        />
      )}
    </main>
  );
}
