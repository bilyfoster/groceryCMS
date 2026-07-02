"use client";

import { useEffect, useState } from "react";
import { fetchAdminSettings, fetchAdminTaxonomies, updateAdminSettings } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { TaxonomyTerm, TaxonomyType } from "@/types/taxonomy";
import type {
  IntakeCriterion,
  IntakeOption,
  IntakeQuestion,
  IntakeQuestionnaire,
} from "@/types/intake";

const CRITERIA: { value: IntakeCriterion; label: string }[] = [
  { value: "FOCUS_AREA", label: "Focus area" },
  { value: "MODALITY", label: "Modality" },
  { value: "DEMOGRAPHIC", label: "Demographic" },
  { value: "SERVICE_DELIVERY", label: "Service delivery" },
];

const CRITERIA_TAXONOMY_TYPE: Record<
  "FOCUS_AREA" | "MODALITY" | "DEMOGRAPHIC",
  TaxonomyType
> = {
  FOCUS_AREA: "FOCUS_AREA",
  MODALITY: "MODALITY",
  DEMOGRAPHIC: "DEMOGRAPHIC",
};

const SERVICE_DELIVERY_OPTIONS: IntakeOption[] = [
  { value: "VIRTUAL", label: "Virtual" },
  { value: "IN_PERSON", label: "In-person" },
  { value: "HYBRID", label: "No preference" },
];

function generateId() {
  return `q_${Math.random().toString(36).slice(2, 9)}`;
}

function defaultQuestionnaire(terms: TaxonomyTerm[]): IntakeQuestionnaire {
  const byType = (type: TaxonomyType) =>
    terms
      .filter((t) => t.type === type)
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((t) => ({ value: t.id, label: t.label }));

  return {
    title: "Find the right therapist",
    description: "Answer a few anonymous questions and we’ll suggest therapists who may be a good fit.",
    questions: [
      {
        id: generateId(),
        label: "What brings you in?",
        type: "multi",
        criterion: "FOCUS_AREA",
        required: false,
        options: byType("FOCUS_AREA"),
      },
      {
        id: generateId(),
        label: "Are there any approaches you prefer?",
        type: "multi",
        criterion: "MODALITY",
        required: false,
        options: byType("MODALITY"),
      },
      {
        id: generateId(),
        label: "Who is the support for?",
        type: "single",
        criterion: "DEMOGRAPHIC",
        required: false,
        options: byType("DEMOGRAPHIC"),
      },
      {
        id: generateId(),
        label: "How would you prefer to meet?",
        type: "single",
        criterion: "SERVICE_DELIVERY",
        required: false,
        options: SERVICE_DELIVERY_OPTIONS,
      },
    ],
  };
}

function optionsForCriterion(
  criterion: IntakeCriterion,
  terms: TaxonomyTerm[],
): IntakeOption[] {
  if (criterion === "SERVICE_DELIVERY") return SERVICE_DELIVERY_OPTIONS;
  const type = CRITERIA_TAXONOMY_TYPE[criterion];
  return terms
    .filter((t) => t.type === type)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((t) => ({ value: t.id, label: t.label }));
}

export default function AdminIntakePage() {
  const [tenantName, setTenantName] = useState("");
  const [settings, setSettings] = useState<Record<string, unknown> | null>(null);
  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [questionnaire, setQuestionnaire] = useState<IntakeQuestionnaire | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([
      fetchAdminSettings(),
      Promise.all(
        (["FOCUS_AREA", "MODALITY", "DEMOGRAPHIC"] as TaxonomyType[]).map((type) =>
          fetchAdminTaxonomies(type),
        ),
      ),
    ])
      .then(([settingsDto, termGroups]) => {
        const allTerms = termGroups.flat();
        setTenantName(settingsDto.name);
        setSettings(settingsDto.settings);
        setTerms(allTerms);
        const configured = settingsDto.settings.intakeQuestionnaire as IntakeQuestionnaire | undefined;
        setQuestionnaire(configured ?? defaultQuestionnaire(allTerms));
      })
      .catch(() => alert("Failed to load intake configuration"))
      .finally(() => setLoading(false));
  }, []);

  const updateQuestionnaire = (updates: Partial<IntakeQuestionnaire>) => {
    setQuestionnaire((prev) => (prev ? { ...prev, ...updates } : prev));
  };

  const updateQuestion = (index: number, updates: Partial<IntakeQuestion>) => {
    setQuestionnaire((prev) => {
      if (!prev) return prev;
      const questions = [...prev.questions];
      const question = { ...questions[index], ...updates };
      if (updates.criterion && updates.criterion !== questions[index].criterion) {
        question.options = optionsForCriterion(updates.criterion, terms);
      }
      questions[index] = question;
      return { ...prev, questions };
    });
  };

  const updateOption = (questionIndex: number, optionIndex: number, label: string) => {
    setQuestionnaire((prev) => {
      if (!prev) return prev;
      const questions = [...prev.questions];
      const options = [...questions[questionIndex].options];
      options[optionIndex] = { ...options[optionIndex], label };
      questions[questionIndex] = { ...questions[questionIndex], options };
      return { ...prev, questions };
    });
  };

  const addQuestion = () => {
    setQuestionnaire((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        questions: [
          ...prev.questions,
          {
            id: generateId(),
            label: "New question",
            type: "single",
            criterion: "FOCUS_AREA",
            required: false,
            options: optionsForCriterion("FOCUS_AREA", terms),
          },
        ],
      };
    });
  };

  const removeQuestion = (index: number) => {
    setQuestionnaire((prev) => {
      if (!prev) return prev;
      const questions = [...prev.questions];
      questions.splice(index, 1);
      return { ...prev, questions };
    });
  };

  const moveQuestion = (index: number, direction: -1 | 1) => {
    setQuestionnaire((prev) => {
      if (!prev) return prev;
      const questions = [...prev.questions];
      const newIndex = index + direction;
      if (newIndex < 0 || newIndex >= questions.length) return prev;
      const [moved] = questions.splice(index, 1);
      questions.splice(newIndex, 0, moved);
      return { ...prev, questions };
    });
  };

  const handleSave = async () => {
    if (!questionnaire || !settings) return;
    setSaving(true);
    try {
      await updateAdminSettings({
        name: tenantName,
        settings: { ...settings, intakeQuestionnaire: questionnaire },
      });
      alert("Intake questionnaire saved");
    } catch {
      alert("Failed to save intake questionnaire");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="p-4 md:p-8">
        <p className="text-slate-600">Loading…</p>
      </div>
    );
  }

  if (!questionnaire) {
    return (
      <div className="p-4 md:p-8">
        <p className="text-slate-600">Failed to load intake configuration.</p>
      </div>
    );
  }

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Intake questionnaire</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={addQuestion}>
            Add question
          </Button>
          <Button onClick={handleSave} disabled={saving}>
            {saving ? "Saving…" : "Save questionnaire"}
          </Button>
        </div>
      </div>

      <div className="mb-6 grid gap-4 rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
        <div>
          <Label htmlFor="title">Title</Label>
          <Input
            id="title"
            value={questionnaire.title}
            onChange={(e) => updateQuestionnaire({ title: e.target.value })}
          />
        </div>
        <div>
          <Label htmlFor="description">Description</Label>
          <Input
            id="description"
            value={questionnaire.description}
            onChange={(e) => updateQuestionnaire({ description: e.target.value })}
          />
        </div>
      </div>

      <div className="flex flex-col gap-4">
        {questionnaire.questions.map((question, index) => (
          <div
            key={question.id}
            className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-4"
          >
            <div className="mb-4 grid gap-4 sm:grid-cols-2">
              <div className="sm:col-span-2">
                <Label htmlFor={`q-${index}-label`}>Question</Label>
                <Input
                  id={`q-${index}-label`}
                  value={question.label}
                  onChange={(e) => updateQuestion(index, { label: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor={`q-${index}-criterion`}>Maps to</Label>
                <select
                  id={`q-${index}-criterion`}
                  value={question.criterion}
                  onChange={(e) =>
                    updateQuestion(index, { criterion: e.target.value as IntakeCriterion })
                  }
                  className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
                >
                  {CRITERIA.map((c) => (
                    <option key={c.value} value={c.value}>
                      {c.label}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <Label htmlFor={`q-${index}-type`}>Answer type</Label>
                <select
                  id={`q-${index}-type`}
                  value={question.type}
                  onChange={(e) =>
                    updateQuestion(index, {
                      type: e.target.value as "single" | "multi",
                    })
                  }
                  className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
                >
                  <option value="single">Single select</option>
                  <option value="multi">Multi select</option>
                </select>
              </div>
              <div className="flex items-center gap-2">
                <input
                  id={`q-${index}-required`}
                  type="checkbox"
                  checked={question.required}
                  onChange={(e) => updateQuestion(index, { required: e.target.checked })}
                />
                <Label htmlFor={`q-${index}-required`}>Required</Label>
              </div>
            </div>

            <div className="mb-4">
              <Label>Options</Label>
              <div className="mt-2 flex flex-col gap-2">
                {question.options.map((option, optionIndex) => (
                  <div key={option.value} className="flex items-center gap-2">
                    <Input
                      value={option.label}
                      onChange={(e) => updateOption(index, optionIndex, e.target.value)}
                      className="flex-1"
                    />
                    <span className="text-xs text-slate-400">{option.value}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex flex-wrap gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => moveQuestion(index, -1)}
                disabled={index === 0}
              >
                Move up
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => moveQuestion(index, 1)}
                disabled={index === questionnaire.questions.length - 1}
              >
                Move down
              </Button>
              <Button variant="destructive" size="sm" onClick={() => removeQuestion(index)}>
                Remove
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
