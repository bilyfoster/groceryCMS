import type { TaxonomyTerm, TaxonomyType } from "./taxonomy";

export type StockStatus = "IN_STOCK" | "LOW_STOCK" | "OUT_OF_STOCK" | "DISCONTINUED";

export type StoreSection =
  | "PANTRY"
  | "BAKERY"
  | "REFRIGERATED"
  | "FROZEN"
  | "PRODUCE"
  | "FRONT";

export interface ProductSummary {
  id: string;
  name: string;
  slug: string;
  brand: string | null;
  price: number | null;
  unit: string | null;
  photoUrl: string | null;
  stockStatus: StockStatus;
  allergyTypes: TaxonomyTerm[];
  dietTypes: TaxonomyTerm[];
  categories: TaxonomyTerm[];
  sortOrder: number;
}

export interface ProductDetail {
  id: string;
  name: string;
  slug: string;
  brand: string | null;
  description: string | null;
  price: number | null;
  unit: string | null;
  photoUrl: string | null;
  stockStatus: StockStatus;
  storeSection: StoreSection;
  metaTitle: string | null;
  metaDescription: string | null;
  ogImageUrl: string | null;
  canonicalUrl: string | null;
  published: boolean;
  sortOrder: number;
  allergyTypes: TaxonomyTerm[];
  dietTypes: TaxonomyTerm[];
  categories: TaxonomyTerm[];
}

export interface ProductRequest {
  name: string;
  slug: string;
  brand?: string;
  description?: string;
  price?: number;
  unit?: string;
  photoUrl?: string;
  stockStatus: StockStatus;
  storeSection: StoreSection;
  metaTitle?: string;
  metaDescription?: string;
  ogImageUrl?: string;
  canonicalUrl?: string;
  published: boolean;
  sortOrder: number;
  termIds?: string[];
}

export interface ProductFilters {
  allergyType?: string;
  dietType?: string;
  category?: string;
  storeSection?: StoreSection;
  stockStatus?: StockStatus;
  q?: string;
}

export const TAXONOMY_TYPES_BY_FIELD: Record<
  "allergyTypes" | "dietTypes" | "categories",
  TaxonomyType
> = {
  allergyTypes: "ALLERGY_TYPE",
  dietTypes: "DIET_TYPE",
  categories: "PRODUCT_CATEGORY",
};

export const STOCK_STATUS_LABELS: Record<StockStatus, string> = {
  IN_STOCK: "In stock",
  LOW_STOCK: "Low stock",
  OUT_OF_STOCK: "Out of stock",
  DISCONTINUED: "Discontinued",
};

export const STOCK_STATUS_BADGE_COLORS: Record<StockStatus, string> = {
  IN_STOCK: "bg-green-100 text-green-800",
  LOW_STOCK: "bg-yellow-100 text-yellow-800",
  OUT_OF_STOCK: "bg-orange-100 text-orange-800",
  DISCONTINUED: "bg-slate-100 text-slate-800",
};

export const STORE_SECTION_LABELS: Record<StoreSection, string> = {
  PANTRY: "Pantry",
  BAKERY: "Bakery",
  REFRIGERATED: "Refrigerated",
  FROZEN: "Frozen",
  PRODUCE: "Produce",
  FRONT: "Front",
};

export function formatPrice(price: number | null, unit?: string | null): string {
  if (price === null || price === undefined) return "";
  const formatted = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(price);
  return unit ? `${formatted} / ${unit}` : formatted;
}
