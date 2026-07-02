"use client";

import { useEffect, useMemo, useState } from "react";
import {
  createTaxonomyTerm,
  deleteTaxonomyTerm,
  fetchAdminTaxonomies,
  fetchCurrentUser,
  updateTaxonomyTerm,
} from "@/lib/api";
import type { TaxonomyTerm, TaxonomyTermRequest, TaxonomyType } from "@/types/taxonomy";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

const TYPES: { value: TaxonomyType; label: string }[] = [
  { value: "ALLERGY_TYPE", label: "Allergens (free-of claims)" },
  { value: "DIET_TYPE", label: "Diet types" },
  { value: "PRODUCT_CATEGORY", label: "Product categories" },
];

function slugify(value: string): string {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-");
}

interface EditingTerm extends Partial<TaxonomyTermRequest> {
  id?: string;
}

export default function AdminTaxonomiesPage() {
  const [role, setRole] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<TaxonomyType>("ALLERGY_TYPE");
  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<EditingTerm | null>(null);

  const isAdmin = role === "ADMIN";

  useEffect(() => {
    fetchCurrentUser().then((user) => {
      if (user) setRole(user.role);
    });
  }, []);

  useEffect(() => {
    setLoading(true);
    fetchAdminTaxonomies(selectedType)
      .then(setTerms)
      .catch(() => alert("Failed to load taxonomy terms"))
      .finally(() => setLoading(false));
  }, [selectedType]);

  const sortedTerms = useMemo(
    () => [...terms].sort((a, b) => a.sortOrder - b.sortOrder || a.label.localeCompare(b.label)),
    [terms],
  );

  const resetEditing = () => {
    setEditing(null);
  };

  const handleSave = async () => {
    if (!editing?.label || !editing?.slug) return;
    const payload: TaxonomyTermRequest = {
      type: selectedType,
      label: editing.label,
      slug: editing.slug,
      description: editing.description,
      sortOrder: editing.sortOrder ?? 0,
      active: editing.active ?? true,
    };
    try {
      if (editing.id) {
        await updateTaxonomyTerm(editing.id, payload);
      } else {
        await createTaxonomyTerm(payload);
      }
      resetEditing();
      const updated = await fetchAdminTaxonomies(selectedType);
      setTerms(updated);
    } catch {
      alert("Failed to save taxonomy term");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this taxonomy term?")) return;
    try {
      await deleteTaxonomyTerm(id);
      const updated = await fetchAdminTaxonomies(selectedType);
      setTerms(updated);
    } catch {
      alert("Failed to delete taxonomy term");
    }
  };

  const startNew = () => {
    setEditing({
      type: selectedType,
      label: "",
      slug: "",
      description: "",
      sortOrder: terms.length,
      active: true,
    });
  };

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Taxonomies</h1>
        {isAdmin && (
          <Button onClick={startNew} aria-label="Add taxonomy term">
            Add term
          </Button>
        )}
      </div>

      <div className="mb-6 flex flex-wrap gap-2" role="tablist" aria-label="Taxonomy type">
        {TYPES.map(({ value, label }) => {
          const active = value === selectedType;
          return (
            <button
              key={value}
              role="tab"
              aria-selected={active}
              onClick={() => {
                setSelectedType(value);
                resetEditing();
              }}
              className={`rounded-[var(--border-radius)] px-4 py-2 text-sm font-medium ${
                active
                  ? "bg-[var(--color-primary)] text-white"
                  : "border border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
              }`}
            >
              {label}
            </button>
          );
        })}
      </div>

      {editing && (
        <div className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4">
          <h2 className="mb-4 text-lg font-semibold">
            {editing.id ? "Edit taxonomy term" : "New taxonomy term"}
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <Label htmlFor="label">Label</Label>
              <Input
                id="label"
                value={editing.label || ""}
                onChange={(e) => {
                  const label = e.target.value;
                  const updates: EditingTerm = { ...editing, label };
                  if (!editing.id && (!editing.slug || editing.slug === slugify(editing.label || ""))) {
                    updates.slug = slugify(label);
                  }
                  setEditing(updates);
                }}
              />
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="slug">Slug</Label>
              <Input
                id="slug"
                value={editing.slug || ""}
                onChange={(e) => setEditing({ ...editing, slug: e.target.value })}
                onBlur={(e) => setEditing({ ...editing, slug: slugify(e.target.value) })}
              />
            </div>
            <div>
              <Label htmlFor="sortOrder">Sort order</Label>
              <Input
                id="sortOrder"
                type="number"
                value={editing.sortOrder ?? 0}
                onChange={(e) =>
                  setEditing({ ...editing, sortOrder: parseInt(e.target.value, 10) || 0 })
                }
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                id="active"
                type="checkbox"
                checked={editing.active ?? true}
                onChange={(e) => setEditing({ ...editing, active: e.target.checked })}
              />
              <Label htmlFor="active">Active</Label>
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                rows={3}
                value={editing.description || ""}
                onChange={(e) => setEditing({ ...editing, description: e.target.value })}
              />
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <Button onClick={handleSave}>Save</Button>
            <Button variant="outline" onClick={resetEditing}>
              Cancel
            </Button>
          </div>
        </div>
      )}

      {loading ? (
        <p className="text-slate-600">Loading…</p>
      ) : sortedTerms.length === 0 ? (
        <p className="text-slate-600">No terms yet.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {sortedTerms.map((term) => (
            <li
              key={term.id}
              className="flex items-start justify-between gap-4 rounded-[var(--border-radius)] border border-slate-200 p-4"
            >
              <div>
                <p className="font-semibold">
                  {term.label}
                  {!term.active && (
                    <span className="ml-2 text-xs text-slate-500">(inactive)</span>
                  )}
                </p>
                <p className="text-sm text-slate-600">/{term.slug}</p>
                {term.description && (
                  <p className="mt-1 line-clamp-2 text-sm text-slate-600">{term.description}</p>
                )}
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    setEditing({
                      id: term.id,
                      type: term.type,
                      label: term.label,
                      slug: term.slug,
                      description: term.description ?? "",
                      sortOrder: term.sortOrder,
                      active: term.active,
                    })
                  }
                >
                  Edit
                </Button>
                {isAdmin && (
                  <Button variant="destructive" size="sm" onClick={() => handleDelete(term.id)}>
                    Delete
                  </Button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
