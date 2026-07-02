"use client";

import { useEffect, useMemo, useState } from "react";
import {
  createProduct,
  deleteProduct,
  fetchAdminProduct,
  fetchAdminProducts,
  fetchAdminTaxonomies,
  publishProduct,
  updateProduct,
} from "@/lib/api";
import type { TaxonomyTerm, TaxonomyType } from "@/types/taxonomy";
import type {
  ProductDetail,
  ProductRequest,
  StockStatus,
  StoreSection,
} from "@/types/product";
import {
  STOCK_STATUS_LABELS,
  STORE_SECTION_LABELS,
  TAXONOMY_TYPES_BY_FIELD,
} from "@/types/product";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ImageInput } from "@/components/admin/ImageInput";

const STOCK_STATUS_OPTIONS: StockStatus[] = [
  "IN_STOCK",
  "LOW_STOCK",
  "OUT_OF_STOCK",
  "DISCONTINUED",
];
const STORE_SECTION_OPTIONS: StoreSection[] = [
  "PANTRY",
  "BAKERY",
  "REFRIGERATED",
  "FROZEN",
  "PRODUCE",
  "FRONT",
];

function slugify(value: string): string {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-");
}

function emptyProduct(): Partial<ProductRequest> {
  return {
    name: "",
    slug: "",
    brand: "",
    description: "",
    price: undefined,
    unit: "",
    photoUrl: "",
    stockStatus: "IN_STOCK",
    storeSection: "PANTRY",
    metaTitle: "",
    metaDescription: "",
    ogImageUrl: "",
    canonicalUrl: "",
    published: false,
    sortOrder: 0,
    termIds: [],
  };
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductDetail[]>([]);
  const [terms, setTerms] = useState<TaxonomyTerm[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Partial<ProductRequest> & { id?: string } | null>(null);

  useEffect(() => {
    loadProducts();
    loadTerms();
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const data = await fetchAdminProducts();
      setProducts(data);
    } catch {
      alert("Failed to load products");
    } finally {
      setLoading(false);
    }
  };

  const loadTerms = async () => {
    try {
      const types: TaxonomyType[] = ["ALLERGY_TYPE", "DIET_TYPE", "PRODUCT_CATEGORY"];
      const results = await Promise.all(types.map((type) => fetchAdminTaxonomies(type)));
      setTerms(results.flat());
    } catch {
      alert("Failed to load taxonomy terms");
    }
  };

  const termsByType = useMemo(() => {
    const map: Record<TaxonomyType, TaxonomyTerm[]> = {
      ALLERGY_TYPE: [],
      DIET_TYPE: [],
      PRODUCT_CATEGORY: [],
    };
    terms.forEach((term) => {
      map[term.type].push(term);
    });
    Object.values(map).forEach((list) => list.sort((a, b) => a.sortOrder - b.sortOrder));
    return map;
  }, [terms]);

  const startNew = () => {
    setEditing({ ...emptyProduct(), sortOrder: products.length });
  };

  const handleEdit = async (id: string) => {
    try {
      const product = await fetchAdminProduct(id);
      setEditing({
        id: product.id,
        name: product.name,
        slug: product.slug,
        brand: product.brand ?? undefined,
        description: product.description ?? undefined,
        price: product.price ?? undefined,
        unit: product.unit ?? undefined,
        photoUrl: product.photoUrl ?? undefined,
        stockStatus: product.stockStatus,
        storeSection: product.storeSection,
        metaTitle: product.metaTitle ?? undefined,
        metaDescription: product.metaDescription ?? undefined,
        ogImageUrl: product.ogImageUrl ?? undefined,
        canonicalUrl: product.canonicalUrl ?? undefined,
        published: product.published,
        sortOrder: product.sortOrder,
        termIds: [
          ...product.allergyTypes.map((t) => t.id),
          ...product.dietTypes.map((t) => t.id),
          ...product.categories.map((t) => t.id),
        ],
      });
    } catch {
      alert("Failed to load product");
    }
  };

  const handleSave = async () => {
    if (!editing?.name || !editing?.slug) return;
    const payload: ProductRequest = {
      name: editing.name,
      slug: editing.slug,
      stockStatus: editing.stockStatus ?? "IN_STOCK",
      storeSection: editing.storeSection ?? "PANTRY",
      published: editing.published ?? false,
      sortOrder: editing.sortOrder ?? 0,
      brand: editing.brand,
      description: editing.description,
      price: editing.price,
      unit: editing.unit,
      photoUrl: editing.photoUrl,
      metaTitle: editing.metaTitle,
      metaDescription: editing.metaDescription,
      ogImageUrl: editing.ogImageUrl,
      canonicalUrl: editing.canonicalUrl,
      termIds: editing.termIds,
    };
    try {
      if (editing.id) {
        await updateProduct(editing.id, payload);
      } else {
        await createProduct(payload);
      }
      setEditing(null);
      await loadProducts();
    } catch {
      alert("Failed to save product");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this product?")) return;
    try {
      await deleteProduct(id);
      await loadProducts();
    } catch {
      alert("Failed to delete product");
    }
  };

  const togglePublish = async (id: string, published: boolean) => {
    try {
      await publishProduct(id, published);
      await loadProducts();
    } catch {
      alert("Failed to update publish status");
    }
  };

  const updateField = <K extends keyof ProductRequest>(
    field: K,
    value: ProductRequest[K],
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

  const sortedProducts = useMemo(
    () =>
      [...products].sort(
        (a, b) => a.sortOrder - b.sortOrder || a.name.localeCompare(b.name),
      ),
    [products],
  );

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Products</h1>
        <Button onClick={startNew}>Add product</Button>
      </div>

      {editing && (
        <div className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4">
          <h2 className="mb-4 text-lg font-semibold">
            {editing.id ? "Edit product" : "New product"}
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label htmlFor="name">Product name</Label>
              <Input
                id="name"
                value={editing.name || ""}
                onChange={(e) => updateField("name", e.target.value)}
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
              <Label htmlFor="brand">Brand</Label>
              <Input
                id="brand"
                value={editing.brand || ""}
                onChange={(e) => updateField("brand", e.target.value)}
              />
            </div>
            <ImageInput
              label="Photo"
              value={editing.photoUrl || ""}
              onChange={(url) => updateField("photoUrl", url)}
            />
            <div>
              <Label htmlFor="price">Price</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                min="0"
                value={editing.price ?? ""}
                onChange={(e) =>
                  updateField(
                    "price",
                    e.target.value ? parseFloat(e.target.value) : undefined,
                  )
                }
              />
            </div>
            <div>
              <Label htmlFor="unit">Unit (e.g. 12 oz)</Label>
              <Input
                id="unit"
                value={editing.unit || ""}
                onChange={(e) => updateField("unit", e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="stockStatus">Stock status</Label>
              <select
                id="stockStatus"
                value={editing.stockStatus}
                onChange={(e) => updateField("stockStatus", e.target.value as StockStatus)}
                className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
              >
                {STOCK_STATUS_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {STOCK_STATUS_LABELS[opt]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="storeSection">Store section</Label>
              <select
                id="storeSection"
                value={editing.storeSection}
                onChange={(e) => updateField("storeSection", e.target.value as StoreSection)}
                className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
              >
                {STORE_SECTION_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {STORE_SECTION_LABELS[opt]}
                  </option>
                ))}
              </select>
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
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                rows={4}
                value={editing.description || ""}
                onChange={(e) => updateField("description", e.target.value)}
              />
            </div>

            {(Object.keys(TAXONOMY_TYPES_BY_FIELD) as Array<keyof typeof TAXONOMY_TYPES_BY_FIELD>).map(
              (field) => {
                const type = TAXONOMY_TYPES_BY_FIELD[field];
                const label =
                  field === "allergyTypes"
                    ? "Free of (allergens)"
                    : field === "dietTypes"
                      ? "Diet types"
                      : "Categories";
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
      ) : sortedProducts.length === 0 ? (
        <p className="text-slate-600">No products yet.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {sortedProducts.map((p) => (
            <li
              key={p.id}
              className="flex items-start justify-between gap-4 rounded-[var(--border-radius)] border border-slate-200 p-4"
            >
              <div>
                <p className="font-semibold">
                  {p.name}
                  {!p.published && (
                    <span className="ml-2 text-xs text-slate-500">(draft)</span>
                  )}
                </p>
                <p className="text-sm text-slate-600">
                  {p.brand}
                  {p.brand && " • "}
                  {STOCK_STATUS_LABELS[p.stockStatus]}
                </p>
                <p className="text-sm text-slate-600">/{p.slug}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" size="sm" onClick={() => handleEdit(p.id)}>
                  Edit
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => togglePublish(p.id, !p.published)}
                >
                  {p.published ? "Unpublish" : "Publish"}
                </Button>
                <Button variant="destructive" size="sm" onClick={() => handleDelete(p.id)}>
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
