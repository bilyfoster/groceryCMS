"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deleteMedia, fetchAdminMedia, mediaUrl } from "@/lib/api";
import { MediaUploader } from "@/components/admin/MediaUploader";
import { Button } from "@/components/ui/button";

export default function AdminMediaPage() {
  const queryClient = useQueryClient();
  const { data: files = [], refetch } = useQuery({
    queryKey: ["admin-media"],
    queryFn: fetchAdminMedia,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteMedia,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-media"] }),
  });

  const { data: siteImages = [] } = useQuery<string[]>({
    queryKey: ["site-images"],
    queryFn: () => fetch("/site-images").then((r) => r.json()),
  });

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Media</h1>
        <div className="flex gap-2">
          <MediaUploader onUploaded={() => refetch()} />
          <Button variant="outline" onClick={() => refetch()}>
            Refresh
          </Button>
        </div>
      </div>
      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {files.map((file) => (
          <li
            key={file.id}
            className="flex flex-col gap-2 rounded-[var(--border-radius)] border border-slate-200 p-3"
          >
            <p className="truncate text-sm font-medium">{file.filename}</p>
            <p className="text-xs text-slate-500">{file.mimeType}</p>
            <code className="break-all text-xs">{mediaUrl(file.storagePath)}</code>
            <Button
              variant="destructive"
              size="sm"
              onClick={() => deleteMutation.mutate(file.id)}
            >
              Delete
            </Button>
          </li>
        ))}
      </ul>
      {files.length === 0 && (
        <p className="text-sm text-slate-500">No uploaded media yet — use Upload above.</p>
      )}

      {siteImages.length > 0 && (
        <div className="mt-10">
          <h2 className="mb-1 text-lg font-semibold">Site images</h2>
          <p className="mb-4 text-sm text-slate-500">
            Images bundled with the site (staff photos, page graphics, logo). Click an image to copy
            its URL for use in a page or block.
          </p>
          <ul className="grid gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {siteImages.map((url) => (
              <li key={url} className="overflow-hidden rounded-[var(--border-radius)] border border-slate-200">
                <button
                  type="button"
                  onClick={() => navigator.clipboard?.writeText(url)}
                  title="Click to copy URL"
                  className="block w-full text-left"
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={url} alt="" className="h-32 w-full bg-slate-50 object-cover" />
                  <code className="block break-all p-2 text-[11px] text-slate-600">{url}</code>
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
