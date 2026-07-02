"use client";

import Image from "next/image";
import { useState } from "react";
import type { GalleryImage, PageDetail } from "@/types/page";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";

interface GalleryPageClientProps {
  page: PageDetail;
  images: GalleryImage[];
}

export function GalleryPageClient({ page, images }: GalleryPageClientProps) {
  const layout = page.layout || "grid-3";
  const [lightbox, setLightbox] = useState<GalleryImage | null>(null);

  const gridClass =
    layout === "masonry"
      ? "columns-1 gap-4 sm:columns-2 lg:columns-3"
      : layout === "grid-4"
        ? "grid gap-4 grid-cols-2 lg:grid-cols-4"
        : "grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3";

  return (
    <article className="mx-auto max-w-6xl px-4 py-8">
      <BlockRenderer blocks={page.blocks} />
      <h1 className="mb-6 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
        {page.title}
      </h1>
      <ul className={gridClass}>
        {images.map((img) => (
          <li
            key={img.id}
            className={layout === "masonry" ? "mb-4 break-inside-avoid" : ""}
          >
            <button
              type="button"
              className="relative block min-h-[44px] w-full overflow-hidden rounded-[var(--border-radius)] text-left"
              onClick={() => layout === "lightbox" && setLightbox(img)}
            >
              <Image
                src={img.url}
                alt={img.altText ?? ""}
                width={600}
                height={400}
                className="h-auto w-full object-cover"
                sizes="(max-width: 768px) 100vw, 33vw"
              />
              {img.caption && (
                <span className="mt-1 block text-sm text-slate-600">{img.caption}</span>
              )}
            </button>
          </li>
        ))}
      </ul>
      {lightbox && (
        <button
          type="button"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4"
          aria-label="Close lightbox"
          onClick={() => setLightbox(null)}
        >
          <Image
            src={lightbox.url}
            alt={lightbox.altText ?? ""}
            width={1200}
            height={800}
            className="max-h-[90vh] w-auto object-contain"
          />
        </button>
      )}
    </article>
  );
}
