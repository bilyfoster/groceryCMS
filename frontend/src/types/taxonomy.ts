export type TaxonomyType = "ALLERGY_TYPE" | "DIET_TYPE" | "PRODUCT_CATEGORY";

export interface TaxonomyTerm {
  id: string;
  type: TaxonomyType;
  label: string;
  slug: string;
  description: string | null;
  sortOrder: number;
  active: boolean;
}

export interface TaxonomyTermRequest {
  type: TaxonomyType;
  label: string;
  slug: string;
  description?: string;
  sortOrder?: number;
  active?: boolean;
}
