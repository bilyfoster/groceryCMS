"use client";

import { useEffect, useMemo, useState } from "react";
import {
  createTherapist,
  deleteTherapist,
  fetchAdminTaxonomies,
  fetchAdminTherapist,
  fetchAdminTherapists,
  publishTherapist,
  updateTherapist,
} from "@/lib/api";
import type { TaxonomyTerm, TaxonomyType } from "@/types/taxonomy";
import type {
  AvailabilityStatus,
  ServiceDelivery,
  TherapistDetail,
  TherapistRequest,
} from "@/types/therapist";
import {
  AVAILABILITY_LABELS,
  SERVICE_DELIVERY_LABELS,
  TAXONOMY_TYPES_BY_FIELD,
} from "@/types/therapist";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ImageInput } from "@/components/admin/ImageInput";

const SERVICE_DELIVERY_OPTIONS: ServiceDelivery[] = ["VIRTUAL", "IN_PERSON", "HYBRID"];
const AVAILABILITY_OPTIONS: AvailabilityStatus[] = [
  "ACCEPTING",
  "LIMITED",
  "WAITLIST",
  "NOT_ACCEPTING",
];

function slugify(value: string): string {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-");
}

function emptyTherapist(): Partial<TherapistRequest> {
  return {
    firstName: "",
    lastName: "",
    credentials: "",
    pronouns: "",
    photoUrl: "",
    slug: "",
    bio: "",
    yearsOfExperience: undefined,
    education: "",
    licensure: "",
    serviceDelivery: "HYBRID",
    availabilityStatus: "ACCEPTING",
    schedulingUrl: "",
    bookingPlatformRef: "",
    metaTitle: "",
    metaDescription: "",
    ogImageUrl: "",
    canonicalUrl: "",
    published: false,
    sortOrder: 0,
    termIds: [],
  };
}

export default function AdminTherapistsPage() {
  const [therapists, setTherapists] = useState<TherapistDetail[]>([]);
  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Partial<TherapistRequest> & { id?: string } | null>(null);

  useEffect(() => {
    loadTherapists();
    loadTerms();
  }, []);

  const loadTherapists = async () => {
    setLoading(true);
    try {
      const data = await fetchAdminTherapists();
      setTherapists(data);
    } catch {
      alert("Failed to load therapists");
    } finally {
      setLoading(false);
    }
  };

  const loadTerms = async () => {
    try {
      const types: TaxonomyType[] = ["FOCUS_AREA", "MODALITY", "DEMOGRAPHIC"];
      const results = await Promise.all(types.map((type) => fetchAdminTaxonomies(type)));
      setTerms(results.flat());
    } catch {
      alert("Failed to load taxonomy terms");
    }
  };

  const termsByType = useMemo(() => {
    const map: Record<TaxonomyType, TaxonomyTerm[]> = {
      FOCUS_AREA: [],
      MODALITY: [],
      DEMOGRAPHIC: [],
    };
    terms.forEach((term) => {
      map[term.type].push(term);
    });
    Object.values(map).forEach((list) => list.sort((a, b) => a.sortOrder - b.sortOrder));
    return map;
  }, [terms]);

  const startNew = () => {
    setEditing({ ...emptyTherapist(), sortOrder: therapists.length });
  };

  const handleEdit = async (id: string) => {
    try {
      const therapist = await fetchAdminTherapist(id);
      setEditing({
        id: therapist.id,
        userId: therapist.userId ?? undefined,
        firstName: therapist.firstName,
        lastName: therapist.lastName,
        credentials: therapist.credentials ?? undefined,
        pronouns: therapist.pronouns ?? undefined,
        photoUrl: therapist.photoUrl ?? undefined,
        slug: therapist.slug,
        bio: therapist.bio ?? undefined,
        yearsOfExperience: therapist.yearsOfExperience ?? undefined,
        education: therapist.education ?? undefined,
        licensure: therapist.licensure ?? undefined,
        serviceDelivery: therapist.serviceDelivery,
        availabilityStatus: therapist.availabilityStatus,
        schedulingUrl: therapist.schedulingUrl ?? undefined,
        bookingPlatformRef: therapist.bookingPlatformRef ?? undefined,
        metaTitle: therapist.metaTitle ?? undefined,
        metaDescription: therapist.metaDescription ?? undefined,
        ogImageUrl: therapist.ogImageUrl ?? undefined,
        canonicalUrl: therapist.canonicalUrl ?? undefined,
        published: therapist.published,
        sortOrder: therapist.sortOrder,
        termIds: [
          ...therapist.focusAreas.map((t) => t.id),
          ...therapist.modalities.map((t) => t.id),
          ...therapist.demographics.map((t) => t.id),
        ],
      });
    } catch {
      alert("Failed to load therapist");
    }
  };

  const handleSave = async () => {
    if (!editing?.firstName || !editing?.lastName || !editing?.slug) return;
    const payload: TherapistRequest = {
      firstName: editing.firstName,
      lastName: editing.lastName,
      slug: editing.slug,
      serviceDelivery: editing.serviceDelivery ?? "HYBRID",
      availabilityStatus: editing.availabilityStatus ?? "ACCEPTING",
      published: editing.published ?? false,
      sortOrder: editing.sortOrder ?? 0,
      userId: editing.userId,
      credentials: editing.credentials,
      pronouns: editing.pronouns,
      photoUrl: editing.photoUrl,
      bio: editing.bio,
      yearsOfExperience: editing.yearsOfExperience,
      education: editing.education,
      licensure: editing.licensure,
      schedulingUrl: editing.schedulingUrl,
      bookingPlatformRef: editing.bookingPlatformRef,
      metaTitle: editing.metaTitle,
      metaDescription: editing.metaDescription,
      ogImageUrl: editing.ogImageUrl,
      canonicalUrl: editing.canonicalUrl,
      termIds: editing.termIds,
    };
    try {
      if (editing.id) {
        await updateTherapist(editing.id, payload);
      } else {
        await createTherapist(payload);
      }
      setEditing(null);
      await loadTherapists();
    } catch {
      alert("Failed to save therapist");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this therapist?")) return;
    try {
      await deleteTherapist(id);
      await loadTherapists();
    } catch {
      alert("Failed to delete therapist");
    }
  };

  const togglePublish = async (id: string, published: boolean) => {
    try {
      await publishTherapist(id, published);
      await loadTherapists();
    } catch {
      alert("Failed to update publish status");
    }
  };

  const updateField = <K extends keyof TherapistRequest>(
    field: K,
    value: TherapistRequest[K],
  ) => {
    setEditing((prev) => (prev ? { ...prev, [field]: value } : prev));
  };

  const toggleTerm = (termId: string, checked: boolean) => {
    setEditing((prev) => {
      if (!prev) return prev;
      const current = new Set(prev.termIds ?? []);
      if (checked) current.add(termId);
      else current.delete(termId);
      return { ...prev, termIds: Array.from(current) };
    });
  };

  const termSelected = (termId: string) => editing?.termIds?.includes(termId) ?? false;

  const sortedTherapists = useMemo(
    () =>
      [...therapists].sort(
        (a, b) => a.sortOrder - b.sortOrder || a.lastName.localeCompare(b.lastName),
      ),
    [therapists],
  );

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Therapists</h1>
        <Button onClick={startNew}>Add therapist</Button>
      </div>

      {editing && (
        <div className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4">
          <h2 className="mb-4 text-lg font-semibold">
            {editing.id ? "Edit therapist" : "New therapist"}
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label htmlFor="firstName">First name</Label>
              <Input
                id="firstName"
                value={editing.firstName || ""}
                onChange={(e) => updateField("firstName", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="lastName">Last name</Label>
              <Input
                id="lastName"
                value={editing.lastName || ""}
                onChange={(e) => {
                  const lastName = e.target.value;
                  const updates: typeof editing = { ...editing, lastName };
                  if (
                    !editing.id &&
                    (!editing.slug ||
                      editing.slug === slugify(`${editing.firstName || ""} ${editing.lastName || ""}`))
                  ) {
                    updates.slug = slugify(`${editing.firstName || ""} ${lastName}`);
                  }
                  setEditing(updates);
                }}
              />
            </div>
            <div>
              <Label htmlFor="slug">Slug</Label>
              <Input
                id="slug"
                value={editing.slug || ""}
                onChange={(e) => updateField("slug", e.target.value)}
                onBlur={(e) => updateField("slug", slugify(e.target.value))}
              />
            </div>
            <div>
              <Label htmlFor="credentials">Credentials</Label>
              <Input
                id="credentials"
                value={editing.credentials || ""}
                onChange={(e) => updateField("credentials", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="pronouns">Pronouns</Label>
              <Input
                id="pronouns"
                value={editing.pronouns || ""}
                onChange={(e) => updateField("pronouns", e.target.value)}
              />
            </div>
            <ImageInput
              label="Photo"
              value={editing.photoUrl || ""}
              onChange={(url) => updateField("photoUrl", url)}
            />
            <div>
              <Label htmlFor="serviceDelivery">Service delivery</Label>
              <select
                id="serviceDelivery"
                value={editing.serviceDelivery}
                onChange={(e) => updateField("serviceDelivery", e.target.value as ServiceDelivery)}
                className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
              >
                {SERVICE_DELIVERY_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {SERVICE_DELIVERY_LABELS[opt]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="availabilityStatus">Availability</Label>
              <select
                id="availabilityStatus"
                value={editing.availabilityStatus}
                onChange={(e) =>
                  updateField("availabilityStatus", e.target.value as AvailabilityStatus)
                }
                className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
              >
                {AVAILABILITY_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {AVAILABILITY_LABELS[opt]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="schedulingUrl">Scheduling URL</Label>
              <Input
                id="schedulingUrl"
                value={editing.schedulingUrl || ""}
                onChange={(e) => updateField("schedulingUrl", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="bookingPlatformRef">Booking platform ref</Label>
              <Input
                id="bookingPlatformRef"
                value={editing.bookingPlatformRef || ""}
                onChange={(e) => updateField("bookingPlatformRef", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="yearsOfExperience">Years of experience</Label>
              <Input
                id="yearsOfExperience"
                type="number"
                value={editing.yearsOfExperience ?? ""}
                onChange={(e) =>
                  updateField(
                    "yearsOfExperience",
                    e.target.value ? parseInt(e.target.value, 10) : undefined,
                  )
                }
              />
            </div>
            <div>
              <Label htmlFor="sortOrder">Sort order</Label>
              <Input
                id="sortOrder"
                type="number"
                value={editing.sortOrder ?? 0}
                onChange={(e) =>
                  updateField("sortOrder", parseInt(e.target.value, 10) || 0)
                }
              />
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="bio">Bio</Label>
              <Textarea
                id="bio"
                rows={4}
                value={editing.bio || ""}
                onChange={(e) => updateField("bio", e.target.value)}
              />
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="education">Education</Label>
              <Input
                id="education"
                value={editing.education || ""}
                onChange={(e) => updateField("education", e.target.value)}
              />
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="licensure">Licensure</Label>
              <Input
                id="licensure"
                value={editing.licensure || ""}
                onChange={(e) => updateField("licensure", e.target.value)}
              />
            </div>

            {(Object.keys(TAXONOMY_TYPES_BY_FIELD) as Array<keyof typeof TAXONOMY_TYPES_BY_FIELD>).map(
              (field) => {
                const type = TAXONOMY_TYPES_BY_FIELD[field];
                const label =
                  field === "focusAreas"
                    ? "Focus areas"
                    : field === "modalities"
                      ? "Modalities"
                      : "Demographics";
                return (
                  <fieldset key={field} className="sm:col-span-2">
                    <legend className="mb-2 text-sm font-medium">{label}</legend>
                    <div className="flex flex-wrap gap-3">
                      {termsByType[type].map((term) => (
                        <label
                          key={term.id}
                          className="inline-flex items-center gap-2 rounded-[var(--border-radius)] border border-slate-200 px-3 py-2"
                        >
                          <input
                            type="checkbox"
                            checked={termSelected(term.id)}
                            onChange={(e) => toggleTerm(term.id, e.target.checked)}
                          />
                          <span className="text-sm">{term.label}</span>
                        </label>
                      ))}
                    </div>
                  </fieldset>
                );
              },
            )}

            <div className="sm:col-span-2">
              <Label htmlFor="metaTitle">SEO title</Label>
              <Input
                id="metaTitle"
                value={editing.metaTitle || ""}
                onChange={(e) => updateField("metaTitle", e.target.value)}
              />
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="metaDescription">Meta description</Label>
              <Input
                id="metaDescription"
                value={editing.metaDescription || ""}
                onChange={(e) => updateField("metaDescription", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="ogImageUrl">OG image URL</Label>
              <Input
                id="ogImageUrl"
                value={editing.ogImageUrl || ""}
                onChange={(e) => updateField("ogImageUrl", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="canonicalUrl">Canonical URL</Label>
              <Input
                id="canonicalUrl"
                value={editing.canonicalUrl || ""}
                onChange={(e) => updateField("canonicalUrl", e.target.value)}
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                id="published"
                type="checkbox"
                checked={editing.published ?? false}
                onChange={(e) => updateField("published", e.target.checked)}
              />
              <Label htmlFor="published">Published</Label>
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <Button onClick={handleSave}>Save</Button>
            <Button variant="outline" onClick={() => setEditing(null)}>
              Cancel
            </Button>
          </div>
        </div>
      )}

      {loading ? (
        <p className="text-slate-600">Loading…</p>
      ) : sortedTherapists.length === 0 ? (
        <p className="text-slate-600">No therapists yet.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {sortedTherapists.map((t) => (
            <li
              key={t.id}
              className="flex items-start justify-between gap-4 rounded-[var(--border-radius)] border border-slate-200 p-4"
            >
              <div>
                <p className="font-semibold">
                  {t.firstName} {t.lastName}
                  {!t.published && (
                    <span className="ml-2 text-xs text-slate-500">(draft)</span>
                  )}
                </p>
                <p className="text-sm text-slate-600">
                  {t.credentials}
                  {t.credentials && " • "}
                  {AVAILABILITY_LABELS[t.availabilityStatus]}
                </p>
                <p className="text-sm text-slate-600">/{t.slug}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" size="sm" onClick={() => handleEdit(t.id)}>
                  Edit
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => togglePublish(t.id, !t.published)}
                >
                  {t.published ? "Unpublish" : "Publish"}
                </Button>
                <Button variant="destructive" size="sm" onClick={() => handleDelete(t.id)}>
                  Delete
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
