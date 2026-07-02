"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import {
  createContentBlock,
  deleteContentBlock,
  fetchAdminPage,
  fetchAdminTaxonomies,
  publishPage,
  reorderBlocks,
  updateContentBlock,
  updatePage,
  ApiError,
} from "@/lib/api";
import type { ContentBlock } from "@/types/page";
import type { TaxonomyTerm } from "@/types/taxonomy";
import { PageBlockEditor, defaultContent } from "@/components/admin/PageBlockEditor";
import { PatternPicker } from "@/components/admin/PatternPicker";
import { RevisionPanel } from "@/components/admin/RevisionPanel";
import { StickySaveBar } from "@/components/admin/StickySaveBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

export default function EditPagePage() {
  const params = useParams();
  const queryClient = useQueryClient();
  const pageId = params.id as string;

  const { data: page, isLoading } = useQuery({
    queryKey: ["admin-page", pageId],
    queryFn: () => fetchAdminPage(pageId),
  });

  const [title, setTitle] = useState("");
  const [layout, setLayout] = useState("default");
  const [navOrder, setNavOrder] = useState<string>("");
  const [metaTitle, setMetaTitle] = useState("");
  const [metaDescription, setMetaDescription] = useState("");
  const [pagePassword, setPagePassword] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [blocks, setBlocks] = useState<ContentBlock[]>([]);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const { data: categories = [] } = useQuery<TaxonomyTerm[]>({
    queryKey: ["admin-product-categories"],
    queryFn: () => fetchAdminTaxonomies("PRODUCT_CATEGORY"),
    enabled: page?.pageType === "service",
  });

  useEffect(() => {
    if (page) {
      setTitle(page.title);
      setLayout(page.layout);
      setNavOrder(page.navOrder != null ? String(page.navOrder) : "");
      setMetaTitle(page.metaTitle ?? "");
      setMetaDescription(page.metaDescription ?? "");
      setCategoryId((page.config?.categoryId as string) ?? "");
      setBlocks(page.blocks);
    }
  }, [page]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      await updatePage(pageId, {
        title,
        layout,
        navOrder: navOrder === "" ? null : Number(navOrder),
        metaTitle: metaTitle || null,
        metaDescription: metaDescription || null,
        config: page?.pageType === "service" ? { categoryId: categoryId || null } : {},
        ...(pagePassword ? { pagePassword } : {}),
      });
      for (const block of blocks) {
        await updateContentBlock(pageId, block.id, {
          content: block.content,
          blockType: block.blockType,
        });
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-page", pageId] });
      queryClient.invalidateQueries({ queryKey: ["admin-pages"] });
      setSaveError(null);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
    onError: (e) => {
      setSaveError(e instanceof ApiError ? e.message : "Save failed");
    },
  });

  const publishMutation = useMutation({
    mutationFn: (published: boolean) => publishPage(pageId, published),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-page", pageId] });
      queryClient.invalidateQueries({ queryKey: ["admin-pages"] });
    },
  });

  const handleAddBlock = async (blockType: string) => {
    const created = await createContentBlock(pageId, {
      blockType,
      content: defaultContent(blockType),
    });
    setBlocks((prev) => [...prev, created]);
  };

  const handleRemoveBlock = async (blockId: string) => {
    await deleteContentBlock(pageId, blockId);
    setBlocks((prev) => prev.filter((b) => b.id !== blockId));
  };

  const handleMoveBlocks = async (orderedIds: string[]) => {
    const reordered = await reorderBlocks(pageId, orderedIds);
    setBlocks(reordered);
  };

  if (isLoading || !page) {
    return (
      <div className="p-8">
        <p className="text-slate-600">Loading page…</p>
      </div>
    );
  }

  const viewHref = page.slug === "home" ? "/" : `/${page.slug}`;

  return (
    <div className="mx-auto max-w-3xl p-4 pb-32 md:p-8">
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <Link href="/admin/pages" className="text-sm text-[var(--color-primary)] hover:underline">
            ← All pages
          </Link>
          <h1 className="mt-1 text-2xl font-bold">Edit page</h1>
          <p className="text-sm text-slate-500">
            /{page.slug}
            {page.published ? (
              <span className="ml-2 rounded bg-green-100 px-2 py-0.5 text-green-800">Published</span>
            ) : (
              <span className="ml-2 rounded bg-amber-100 px-2 py-0.5 text-amber-800">Draft</span>
            )}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link
            href={viewHref}
            target="_blank"
            className="inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] border border-slate-300 px-4 text-sm"
          >
            View page
          </Link>
          {page.published ? (
            <Button
              type="button"
              variant="outline"
              onClick={() => publishMutation.mutate(false)}
              disabled={publishMutation.isPending}
            >
              Unpublish
            </Button>
          ) : (
            <Button
              type="button"
              onClick={() => publishMutation.mutate(true)}
              disabled={publishMutation.isPending}
            >
              Publish
            </Button>
          )}
        </div>
      </div>

      <section className="mb-8 flex flex-col gap-4 rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold">Page settings</h2>
        <div>
          <Label htmlFor="title">Title</Label>
          <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} autoFocus />
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <Label htmlFor="layout">Layout</Label>
            <Input id="layout" value={layout} onChange={(e) => setLayout(e.target.value)} />
          </div>
          <div>
            <Label htmlFor="navOrder">Nav order (blank = hidden)</Label>
            <Input
              id="navOrder"
              type="number"
              inputMode="numeric"
              value={navOrder}
              onChange={(e) => setNavOrder(e.target.value)}
            />
          </div>
        </div>
        <div>
          <Label htmlFor="metaTitle">SEO title</Label>
          <Input id="metaTitle" value={metaTitle} onChange={(e) => setMetaTitle(e.target.value)} />
        </div>
        <div>
          <Label htmlFor="pagePassword">Page password</Label>
          <Input
            id="pagePassword"
            type="password"
            placeholder="Leave blank to keep unchanged"
            value={pagePassword}
            onChange={(e) => setPagePassword(e.target.value)}
            autoComplete="new-password"
          />
        </div>
        <div>
          <Label htmlFor="metaDescription">SEO description</Label>
          <Textarea
            id="metaDescription"
            rows={2}
            value={metaDescription}
            onChange={(e) => setMetaDescription(e.target.value)}
          />
        </div>
        {page.pageType === "service" && (
          <div>
            <Label htmlFor="category">Related product category</Label>
            <select
              id="category"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <option value="">None</option>
              {categories.map((term) => (
                <option key={term.id} value={term.id}>
                  {term.label}
                </option>
              ))}
            </select>
            <p className="mt-1 text-xs text-slate-500">
              Related products in this category will appear on the service page.
            </p>
          </div>
        )}
      </section>

      <section className="mb-8 rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
        <h2 className="mb-3 text-lg font-semibold">Revision history</h2>
        <RevisionPanel
          entityType="page"
          entityId={pageId}
          onRestored={() => {
            queryClient.invalidateQueries({ queryKey: ["admin-page", pageId] });
          }}
        />
      </section>

      <section className="mb-4">
        <h2 className="mb-3 text-lg font-semibold">Page content</h2>
        <p className="mb-4 text-sm text-slate-500">
          Add blocks to build your page — like WordPress blocks. Save when you are done.
        </p>
        <div className="mb-4">
          <PatternPicker
            onInsert={async (patternBlocks) => {
              for (const b of patternBlocks) {
                const created = await createContentBlock(pageId, {
                  blockType: b.blockType,
                  content: b.content,
                });
                setBlocks((prev) => [...prev, created]);
              }
            }}
          />
        </div>
        <PageBlockEditor
          blocks={blocks}
          onChange={setBlocks}
          onAdd={handleAddBlock}
          onRemove={handleRemoveBlock}
          onMove={handleMoveBlocks}
        />
      </section>

      {saveError && <p className="mb-4 text-sm text-red-600">{saveError}</p>}
      {saved && <p className="mb-4 text-sm text-green-600">Saved.</p>}

      <StickySaveBar
        onSave={() => saveMutation.mutate()}
        saving={saveMutation.isPending}
        label={saveMutation.isPending ? "Saving…" : "Save changes"}
      />
    </div>
  );
}
