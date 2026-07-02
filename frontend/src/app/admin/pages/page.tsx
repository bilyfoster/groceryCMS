"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  createPage,
  deletePage,
  fetchAdminPages,
  publishPage,
  ApiError,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function AdminPagesPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: pages = [], isLoading, refetch } = useQuery({
    queryKey: ["admin-pages"],
    queryFn: fetchAdminPages,
  });

  const [slug, setSlug] = useState("");
  const [title, setTitle] = useState("");
  const [pageType, setPageType] = useState("custom");

  const createMutation = useMutation({
    mutationFn: () => createPage({ slug, title, pageType, layout: "default" }),
    onSuccess: (page) => {
      queryClient.invalidateQueries({ queryKey: ["admin-pages"] });
      setSlug("");
      setTitle("");
      router.push(`/admin/pages/${page.id}`);
    },
  });

  const publishMutation = useMutation({
    mutationFn: ({ id, published }: { id: string; published: boolean }) =>
      publishPage(id, published),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-pages"] }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deletePage(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-pages"] }),
  });

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">Pages</h1>
          <p className="text-sm text-slate-500">
            Create and edit site pages — your WordPress-style Pages screen.
          </p>
        </div>
        <Button type="button" variant="outline" onClick={() => refetch()}>
          Refresh
        </Button>
      </div>

      <Card className="mb-8">
        <CardHeader>
          <CardTitle>Add new page</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <Label htmlFor="slug">URL slug</Label>
              <Input
                id="slug"
                placeholder="about-us"
                value={slug}
                onChange={(e) => setSlug(e.target.value.toLowerCase().replace(/\s+/g, "-"))}
                autoFocus
              />
            </div>
            <div>
              <Label htmlFor="title">Title</Label>
              <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>
          </div>
          <div>
            <Label htmlFor="pageType">Page type</Label>
            <select
              id="pageType"
              value={pageType}
              onChange={(e) => setPageType(e.target.value)}
              className="flex min-h-[44px] w-full rounded-[var(--border-radius)] border border-slate-300 px-3"
            >
              {["home", "blog", "faq", "contact", "staff", "gallery", "service", "custom"].map(
                (t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ),
              )}
            </select>
          </div>
          <Button
            type="button"
            onClick={() => createMutation.mutate()}
            disabled={!slug || !title || createMutation.isPending}
          >
            Create & edit
          </Button>
          {createMutation.error && (
            <p className="text-sm text-red-600">
              {(createMutation.error as ApiError).message}
            </p>
          )}
        </CardContent>
      </Card>

      {isLoading ? (
        <p>Loading…</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {pages.map((page) => (
            <li
              key={page.id}
              className="flex flex-col gap-2 rounded-[var(--border-radius)] border border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div>
                <p className="font-medium">{page.title}</p>
                <p className="text-sm text-slate-500">
                  /{page.slug} · {page.pageType}
                  {page.published ? (
                    <span className="ml-2 text-green-700">Published</span>
                  ) : (
                    <span className="ml-2 text-amber-700">Draft</span>
                  )}
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Link
                  href={`/admin/pages/${page.id}`}
                  className="inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-4 text-sm font-medium text-white"
                >
                  Edit
                </Link>
                <Link
                  href={page.slug === "home" ? "/" : `/${page.slug}`}
                  target="_blank"
                  className="inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] border border-slate-300 px-3 text-sm"
                >
                  View
                </Link>
                {!page.published && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => publishMutation.mutate({ id: page.id, published: true })}
                  >
                    Publish
                  </Button>
                )}
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => {
                    if (confirm(`Delete "${page.title}"?`)) {
                      deleteMutation.mutate(page.id);
                    }
                  }}
                >
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
