"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import {
  fetchAdminBlogPost,
  publishBlogPost,
  updateBlogPost,
  ApiError,
} from "@/lib/api";
import { StickySaveBar } from "@/components/admin/StickySaveBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const RichTextEditor = dynamic(
  () => import("@/components/admin/RichTextEditor").then((m) => m.RichTextEditor),
  { ssr: false }
);

export default function EditBlogPostPage() {
  const params = useParams();
  const queryClient = useQueryClient();
  const postId = params.id as string;

  const { data: post, isLoading } = useQuery({
    queryKey: ["admin-blog-post", postId],
    queryFn: () => fetchAdminBlogPost(postId),
  });

  const [title, setTitle] = useState("");
  const [slug, setSlug] = useState("");
  const [excerpt, setExcerpt] = useState("");
  const [featuredImage, setFeaturedImage] = useState("");
  const [metaTitle, setMetaTitle] = useState("");
  const [metaDescription, setMetaDescription] = useState("");
  const [rawBody, setRawBody] = useState("[]");
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (post) {
      setTitle(post.title);
      setSlug(post.slug);
      setExcerpt(post.excerpt ?? "");
      setFeaturedImage(post.featuredImage ?? "");
      setMetaTitle(post.metaTitle ?? "");
      setMetaDescription(post.metaDescription ?? "");
      const storedBody = post.bodyHtml ? post.bodyHtml.trim() : "[]";
      const isJson = storedBody.startsWith("[") || storedBody.startsWith("{");
      setRawBody(isJson ? storedBody : "[]");
    }
  }, [post]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      await updateBlogPost(postId, {
        title,
        slug,
        excerpt: excerpt || null,
        body: rawBody,
        featuredImage: featuredImage || null,
        metaTitle: metaTitle || null,
        metaDescription: metaDescription || null,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-blog-post", postId] });
      queryClient.invalidateQueries({ queryKey: ["admin-blog"] });
      setSaveError(null);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
    onError: (e) => {
      setSaveError(e instanceof ApiError ? e.message : "Save failed");
    },
  });

  const publishMutation = useMutation({
    mutationFn: (published: boolean) => publishBlogPost(postId, published),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-blog-post", postId] });
      queryClient.invalidateQueries({ queryKey: ["admin-blog"] });
    },
  });

  if (isLoading || !post) {
    return (
      <div className="p-8">
        <p className="text-slate-600">Loading post…</p>
      </div>
    );
  }

  const isLegacyHtml =
    post.bodyHtml &&
    !post.bodyHtml.trim().startsWith("[") &&
    !post.bodyHtml.trim().startsWith("{");

  return (
    <div className="mx-auto max-w-3xl p-4 pb-32 md:p-8">
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <Link
            href="/admin/blog"
            className="text-sm text-[var(--color-primary)] hover:underline"
          >
            ← All posts
          </Link>
          <h1 className="mt-1 text-2xl font-bold">Edit blog post</h1>
          <p className="text-sm text-slate-500">
            /blog/{post.slug}
            {post.published ? (
              <span className="ml-2 rounded bg-green-100 px-2 py-0.5 text-green-800">
                Published
              </span>
            ) : (
              <span className="ml-2 rounded bg-amber-100 px-2 py-0.5 text-amber-800">
                Draft
              </span>
            )}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link
            href={`/blog/${post.slug}`}
            target="_blank"
            className="inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] border border-slate-300 px-4 text-sm"
          >
            View post
          </Link>
          {post.published ? (
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
        <h2 className="text-lg font-semibold">Post settings</h2>
        <div>
          <Label htmlFor="title">Title</Label>
          <Input
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            autoFocus
          />
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <Label htmlFor="slug">Slug</Label>
            <Input id="slug" value={slug} onChange={(e) => setSlug(e.target.value)} />
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
        </div>
        <div>
          <Label htmlFor="excerpt">Excerpt</Label>
          <Input
            id="excerpt"
            value={excerpt}
            onChange={(e) => setExcerpt(e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="metaTitle">SEO title</Label>
          <Input
            id="metaTitle"
            value={metaTitle}
            onChange={(e) => setMetaTitle(e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="metaDescription">SEO description</Label>
          <Input
            id="metaDescription"
            value={metaDescription}
            onChange={(e) => setMetaDescription(e.target.value)}
          />
        </div>
      </section>

      <section className="mb-8 rounded-[var(--border-radius)] border border-slate-200 bg-white p-4">
        <h2 className="mb-3 text-lg font-semibold">Post body</h2>
        {isLegacyHtml && (
          <p className="mb-3 rounded bg-amber-50 px-3 py-2 text-sm text-amber-800">
            This post was created with legacy HTML. The editor below starts empty;
            saving will replace the body.
          </p>
        )}
        <RichTextEditor initialContent={rawBody} onChange={setRawBody} />
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
