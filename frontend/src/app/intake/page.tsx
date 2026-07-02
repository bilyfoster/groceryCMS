"use client";

import { useEffect, useState } from "react";
import { fetchIntakeQuestionnaire, submitIntakeMatch } from "@/lib/api";
import { trackEvent } from "@/lib/analytics";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import { ProductRecommendationList } from "@/components/ProductRecommendationList";
import { Button } from "@/components/ui/button";
import type { IntakeAnswers, IntakeQuestion, IntakeQuestionnaire } from "@/types/intake";
import type { MatchResponse } from "@/types/match";

export default function IntakePage() {
  const [questionnaire, setQuestionnaire] = useState<IntakeQuestionnaire | null>(null);
  const [loading, setLoading] = useState(true);
  const [answers, setAnswers] = useState<IntakeAnswers>({});
  const [results, setResults] = useState<MatchResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    trackEvent("intake_start");
    fetchIntakeQuestionnaire()
      .then(setQuestionnaire)
      .catch(() => alert("Failed to load intake form"))
      .finally(() => setLoading(false));
  }, []);

  const toggleAnswer = (question: IntakeQuestion, value: string, checked: boolean) => {
    setAnswers((prev) => {
      const current = new Set(prev[question.id] ?? []);
      if (checked) {
        if (question.type === "single") {
          return { ...prev, [question.id]: [value] };
        }
        current.add(value);
      } else {
        current.delete(value);
      }
      return { ...prev, [question.id]: Array.from(current) };
    });
  };

  const handleSubmit = async () => {
    if (!questionnaire) return;
    setSubmitting(true);
    try {
      const response = await submitIntakeMatch({ answers });
      setResults(response);
      trackEvent("intake_complete", { matchCount: response.matches.length });
    } catch {
      alert("Failed to get recommendations. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = () => {
    setAnswers({});
    setResults(null);
    trackEvent("intake_start");
  };

  if (loading) {
    return (
      <main className="mx-auto max-w-2xl px-4 py-12 text-center">
        <p className="text-slate-600">Loading…</p>
      </main>
    );
  }

  if (!questionnaire) {
    return (
      <main className="mx-auto max-w-2xl px-4 py-12 text-center">
        <p className="text-slate-600">Intake form is not available.</p>
      </main>
    );
  }

  if (results) {
    return (
      <main className="mx-auto max-w-2xl px-4 py-8 md:py-12">
        <ProductRecommendationList
          matches={results.matches}
          awarenessNotes={results.awarenessNotes}
          onReset={handleReset}
        />
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl px-4 py-8 md:py-12">
      <AnalyticsTracker eventType="intake_start" />
      <h1 className="mb-2 text-3xl font-bold">{questionnaire.title}</h1>
      {questionnaire.description && (
        <p className="mb-8 text-slate-600">{questionnaire.description}</p>
      )}

      <div className="flex flex-col gap-6">
        {questionnaire.questions.map((question) => {
          const selected = answers[question.id] ?? [];
          const inputType = question.type === "multi" ? "checkbox" : "radio";

          return (
            <fieldset
              key={question.id}
              className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6"
            >
              <legend className="mb-2 text-lg font-semibold">
                {question.label}
                {question.required && <span className="ml-1 text-red-500">*</span>}
              </legend>
              <div className="flex flex-wrap gap-3">
                {question.options.map((option) => {
                  const checked = selected.includes(option.value);
                  return (
                    <label
                      key={option.value}
                      className={`inline-flex cursor-pointer items-center gap-2 rounded-[var(--border-radius)] border px-4 py-2 text-sm hover:bg-slate-50 ${
                        checked
                          ? "border-[var(--color-primary)] bg-[var(--color-primary)]/10"
                          : "border-slate-200"
                      }`}
                    >
                      <input
                        type={inputType}
                        name={question.id}
                        value={option.value}
                        checked={checked}
                        onChange={(e) =>
                          toggleAnswer(question, option.value, e.target.checked)
                        }
                        className="sr-only"
                      />
                      <span>{option.label}</span>
                    </label>
                  );
                })}
              </div>
            </fieldset>
          );
        })}

        <div className="flex justify-end">
          <Button onClick={handleSubmit} disabled={submitting}>
            {submitting ? "Finding recommendations…" : "See my recommendations"}
          </Button>
        </div>
      </div>
    </main>
  );
}
