"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  createCategory,
  deleteCategory,
  fetchCategories,
  type CategoryTree,
  ApiError,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

function CategoryRow({
  node,
  depth,
  onDelete,
}: {
  node: CategoryTree;
  depth: number;
  onDelete: (id: string) => void;
}) {
  return (
    <>
      <li
        className="flex items-center justify-between gap-2 border-b border-slate-100 py-2"
        style={{ paddingLeft: depth * 16 }}
      >
        <span>
          <strong>{node.name}</strong>
          <span className="ml-2 text-xs text-slate-500">/{node.slug}</span>
        </span>
        <Button type="button" variant="ghost" size="sm" onClick={() => onDelete(node.id)}>
          Delete
        </Button>
      </li>
      {node.children.map((child) => (
        <CategoryRow key={child.id} node={child} depth={depth + 1} onDelete={onDelete} />
      ))}
    </>
  );
}

export default function AdminCategoriesPage() {
  const queryClient = useQueryClient();
  const { data: categories = [] } = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
  });

  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [error, setError] = useState<string | null>(null);

  const createMutation = useMutation({
    mutationFn: () => createCategory({ name, slug }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] });
      setName("");
      setSlug("");
      setError(null);
    },
    onError: (e) => setError(e instanceof ApiError ? e.message : "Failed"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteCategory(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["categories"] }),
  });

  return (
    <div className="p-4 pb-28 md:p-8">
      <h1 className="mb-6 text-2xl font-bold">Categories</h1>

      <Card className="mb-8">
        <CardHeader>
          <CardTitle>New category</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="flex-1">
            <Label htmlFor="catName">Name</Label>
            <Input id="catName" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="flex-1">
            <Label htmlFor="catSlug">Slug</Label>
            <Input id="catSlug" value={slug} onChange={(e) => setSlug(e.target.value)} />
          </div>
          <Button
            type="button"
            onClick={() => createMutation.mutate()}
            disabled={!name || !slug || createMutation.isPending}
          >
            Add
          </Button>
        </CardContent>
        {error && <p className="px-6 pb-4 text-sm text-red-600">{error}</p>}
      </Card>

      <ul className="rounded-lg border border-slate-200 bg-white px-4">
        {categories.map((c) => (
          <CategoryRow
            key={c.id}
            node={c}
            depth={0}
            onDelete={(id) => deleteMutation.mutate(id)}
          />
        ))}
      </ul>
    </div>
  );
}
