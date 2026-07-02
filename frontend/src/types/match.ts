import type { StockStatus } from "./product";

export interface IntakeRequest {
  avoidedAllergies?: string[];
  preferredDiets?: string[];
  preferredCategories?: string[];
  symptoms?: string[];
  contactEmail?: string;
}

export interface MatchResult {
  productId: string;
  slug: string;
  name: string;
  brand: string | null;
  price: number | null;
  unit: string | null;
  photoUrl: string | null;
  stockStatus: StockStatus;
  score: number;
  rank: number;
  explanations: string[];
}

export interface MatchResponse {
  matches: MatchResult[];
  awarenessNotes: string[];
}
