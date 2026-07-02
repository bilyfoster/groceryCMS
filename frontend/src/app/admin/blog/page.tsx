"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import dynamic from "next/dynamic";
import { useState } from "react";
import {
  createBlogPost,
  deleteBlogPost,
  fetchAdminBlogPosts,
  fetchAdminPages,
  fetchCategories,
  publishBlogPost,
} from "@/lib/api";
import { StickySaveBar } from "@/components/admin/StickySaveBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const RichTextEditor = dynamic(
  () => import("@/components/admin/RichTextEditor").then((m) => m.RichTextEditor),
  { ssr: false }
);

export default function AdminBlogPage() {
  const queryClient = useQueryClient();
  const { data: posts = [] } = useQuery({
    queryKey: ["admin-blog"],
    queryFn: fetchAdminBlogPosts,
  });
  const { data: pages = [] } = useQuery({
    queryKey: ["admin-pages"],
    queryFn: fetchAdminPages,
  });
  const { data: categories = [] } = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
  });

  const blogPage = pages.find((p) => p.pageType === "blog");

  const [slug, setSlug] = useState("");
  const [title, setTitle] = useState("");
  const [excerpt, setExcerpt] = useState("");
  const [body, setBody] = useState("[]");
  const [featuredImage, setFeaturedImage] = useState("");
  const [sticky, setSticky] = useState(false);
  const [categoryIds, setCategoryIds] = useState<string[]>([]);

  const createMutation = useMutation({
    mutationFn: () => {
      if (!blogPage) throw new Error("Create a blog page first");
      return createBlogPost({
        pageId: blogPage.id,
        slug,
        title,
        excerpt,
        body,
        featuredImage: featuredImage || undefined,
        sticky,
        categoryIds,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-blog"] });
      setSlug("");
      setTitle("");
      setExcerpt("");
      setBody("[]");
      setFeaturedImage("");
    },
  });

  const publishMutation = useMutation({
    mutationFn: ({ id, published }: { id: string; published: boolean }) =>
      publishBlogPost(id, published),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-blog"] }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteBlogPost(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-blog"] }),
  });

  return (
    <div className="p-4 pb-28 md:p-8 md:pb-8">
      <h1 className="mb-6 text-2xl font-bold">Blog</h1>

      {!blogPage && (
        <p className="mb-4 text-sm text-amber-700">
          Create a published page with type &quot;blog&quot; before adding posts.
        </p>
      )}

      <Card className="mb-8">
        <CardHeader>
          <CardTitle>New post</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div>
            <Label htmlFor="post-slug">Slug</Label>
            <Input id="post-slug" value={slug} onChange={(e) => setSlug(e.target.value)} autoFocus />
          </div>
          <div>
            <Label htmlFor="post-title">Title</Label>
            <Input id="post-title" value={title} onChange={(e) => setTitle(e.target.value)} />
          </div>
          <div>
            <Label htmlFor="excerpt">Excerpt</Label>
            <Input id="excerpt" value={excerpt} onChange={(e) => setExcerpt(e.target.value)} />
          </div>
          <div>
            <Label htmlFor="featuredImage">Featured image URL</Label>
            <Input
              id="featuredImage"
              value={featuredImage}
              onChange={(e) => setFeaturedImage(e.target.value)}
              placeholder="/images/photo.jpg"
            />
          </div>
          <div>
            <Label>Body</Label>
            <RichTextEditor initialContent={body} onChange={setBody} />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={sticky}
              onChange={(e) => setSticky(e.target.checked)}
            />
            Stick to top of blog
          </label>
          {categories.length > 0 && (
            <div>
              <Label>Categories</Label>
              <select
                multiple
                className="mt-1 min-h-[88px] w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={categoryIds}
                onChange={(e) =>
                  setCategoryIds(Array.from(e.target.selectedOptions, (o) => o.value))
                }
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          )}
        </CardContent>
      </Card>

      <ul className="mb-8 flex flex-col gap-3">
        {posts.map((post) => (
          <li
            key={post.id}
            className="flex flex-col gap-2 rounded-[var(--border-radius)] border border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div>
              <p className="font-medium">{post.title}</p>
              <p className="text-sm text-slate-500">
                /blog/{post.slug}
                {post.sticky && " · Pinned"}
                {!post.published && " · Draft"}
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                size="sm"
                variant="outline"
                onClick={() => publishMutation.mutate({ id: post.id, published: true })}
              >
                Publish
              </Button>
              <Link
                href={`/admin/blog/${post.id}`}
                className="inline-flex h-9 items-center rounded-[var(--border-radius)] border border-slate-300 bg-transparent px-3 text-xs font-medium hover:bg-slate-50"
              >
                Edit
              </Link>
              <Button
                size="sm"
                variant="destructive"
                onClick={() => deleteMutation.mutate(post.id)}
              >
                Delete
              </Button>
            </div>
          </li>
        ))}
      </ul>

      <StickySaveBar
        label="Create post"
        saving={createMutation.isPending}
        onSave={() => createMutation.mutate()}
      />
    </div>
  );
}
