import { fetchGallery } from "@/lib/api";
import { GalleryPageClient } from "@/components/page-types/GalleryPageClient";
import type { PageTypeProps } from "@/components/page-types";

export async function GalleryPage({ page }: PageTypeProps) {
  const images = await fetchGallery(page.id);
  return <GalleryPageClient page={page} images={images} />;
}
