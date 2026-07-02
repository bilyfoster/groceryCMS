import type { AvailabilityStatus, ServiceDelivery } from "./therapist";

export interface IntakeRequest {
  areasOfConcern?: string[];
  preferredModalities?: string[];
  clientDemographic?: string;
  preferredDelivery?: ServiceDelivery;
  contactEmail?: string;
  therapistGenderPreference?: string;
  therapistIdentityPreference?: string;
}

export interface MatchResult {
  therapistId: string;
  slug: string;
  firstName: string;
  lastName: string;
  credentials: string | null;
  photoUrl: string | null;
  availabilityStatus: AvailabilityStatus;
  score: number;
  rank: number;
  explanations: string[];
}

export interface MatchResponse {
  matches: MatchResult[];
}
