"use client";

import { useEffect, useState } from "react";
import { Eye, Heart } from "lucide-react";
import { incrementBlogView, likeBlogPost, unlikeBlogPost } from "@/lib/api";

interface Props {
  slug: string;
  initialViews: number;
  initialLikes: number;
}

export function BlogEngagement({ slug, initialViews, initialLikes }: Props) {
  const [views, setViews] = useState(initialViews);
  const [likes, setLikes] = useState(initialLikes);
  const [liked, setLiked] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    // Count a view once per browser session per post.
    const viewedKey = `viewed:${slug}`;
    if (!sessionStorage.getItem(viewedKey)) {
      sessionStorage.setItem(viewedKey, "1");
      incrementBlogView(slug).then(setViews).catch(() => {});
    }
    setLiked(localStorage.getItem(`liked:${slug}`) === "1");
  }, [slug]);

  const toggleLike = async () => {
    if (busy) return;
    setBusy(true);
    const next = !liked;
    setLiked(next);
    setLikes((n) => Math.max(0, n + (next ? 1 : -1)));
    try {
      const count = next ? await likeBlogPost(slug) : await unlikeBlogPost(slug);
      setLikes(count);
      if (next) localStorage.setItem(`liked:${slug}`, "1");
      else localStorage.removeItem(`liked:${slug}`);
    } catch {
      setLiked(!next);
      setLikes((n) => Math.max(0, n + (next ? -1 : 1)));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex items-center gap-4 text-sm text-slate-500">
      <span className="inline-flex items-center gap-1.5">
        <Eye className="h-4 w-4" aria-hidden="true" />
        {views.toLocaleString()}
        <span className="hidden sm:inline">views</span>
      </span>
      <button
        type="button"
        onClick={toggleLike}
        aria-pressed={liked}
        aria-label={liked ? "Unlike this post" : "Like this post"}
        className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 font-medium transition-colors ${
          liked
            ? "border-[var(--color-primary)] bg-[var(--color-primary)]/10 text-[var(--color-primary)]"
            : "border-slate-200 hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]"
        }`}
      >
        <Heart className={`h-4 w-4 ${liked ? "fill-current" : ""}`} aria-hidden="true" />
        {likes.toLocaleString()}
      </button>
    </div>
  );
}
