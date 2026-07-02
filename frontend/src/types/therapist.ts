import type { TaxonomyTerm, TaxonomyType } from "./taxonomy";

export type ServiceDelivery = "VIRTUAL" | "IN_PERSON" | "HYBRID";

export type AvailabilityStatus = "ACCEPTING" | "LIMITED" | "WAITLIST" | "NOT_ACCEPTING";

export interface TherapistSummary {
  id: string;
  firstName: string;
  lastName: string;
  credentials: string | null;
  pronouns: string | null;
  photoUrl: string | null;
  slug: string;
  availabilityStatus: AvailabilityStatus;
  focusAreas: TaxonomyTerm[];
  modalities: TaxonomyTerm[];
  demographics: TaxonomyTerm[];
  sortOrder: number;
}

export interface TherapistDetail {
  id: string;
  userId: string | null;
  firstName: string;
  lastName: string;
  credentials: string | null;
  pronouns: string | null;
  photoUrl: string | null;
  slug: string;
  bio: string | null;
  yearsOfExperience: number | null;
  education: string | null;
  licensure: string | null;
  serviceDelivery: ServiceDelivery;
  availabilityStatus: AvailabilityStatus;
  schedulingUrl: string | null;
  bookingPlatformRef: string | null;
  metaTitle: string | null;
  metaDescription: string | null;
  ogImageUrl: string | null;
  canonicalUrl: string | null;
  published: boolean;
  sortOrder: number;
  focusAreas: TaxonomyTerm[];
  modalities: TaxonomyTerm[];
  demographics: TaxonomyTerm[];
}

export interface TherapistRequest {
  userId?: string | null;
  firstName: string;
  lastName: string;
  credentials?: string;
  pronouns?: string;
  photoUrl?: string;
  slug: string;
  bio?: string;
  yearsOfExperience?: number;
  education?: string;
  licensure?: string;
  serviceDelivery: ServiceDelivery;
  availabilityStatus: AvailabilityStatus;
  schedulingUrl?: string;
  bookingPlatformRef?: string;
  metaTitle?: string;
  metaDescription?: string;
  ogImageUrl?: string;
  canonicalUrl?: string;
  published: boolean;
  sortOrder: number;
  termIds?: string[];
}

export interface TherapistFilters {
  focusArea?: string;
  modality?: string;
  demographic?: string;
  delivery?: ServiceDelivery;
  availability?: AvailabilityStatus;
  q?: string;
}

export const TAXONOMY_TYPES_BY_FIELD: Record<
  "focusAreas" | "modalities" | "demographics",
  TaxonomyType
> = {
  focusAreas: "FOCUS_AREA",
  modalities: "MODALITY",
  demographics: "DEMOGRAPHIC",
};

export const SERVICE_DELIVERY_LABELS: Record<ServiceDelivery, string> = {
  VIRTUAL: "Virtual",
  IN_PERSON: "In-person",
  HYBRID: "Hybrid",
};

export const AVAILABILITY_LABELS: Record<AvailabilityStatus, string> = {
  ACCEPTING: "Accepting new clients",
  LIMITED: "Limited availability",
  WAITLIST: "Waitlist only",
  NOT_ACCEPTING: "Not accepting clients",
};

export const AVAILABILITY_BADGE_COLORS: Record<AvailabilityStatus, string> = {
  ACCEPTING: "bg-green-100 text-green-800",
  LIMITED: "bg-yellow-100 text-yellow-800",
  WAITLIST: "bg-orange-100 text-orange-800",
  NOT_ACCEPTING: "bg-slate-100 text-slate-800",
};
