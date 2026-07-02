"use client";

import { useRef, useState } from "react";
import { Upload, X, Images } from "lucide-react";
import { uploadMedia, mediaUrl, fetchAdminMedia } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface ImageInputProps {
  label?: string;
  value: string;
  onChange: (url: string) => void;
}

/**
 * Image field with inline upload and a media-library picker. Admins can upload a
 * file, choose an existing image (uploaded media or bundled site images), or
 * paste a URL. Shows a preview.
 */
export function ImageInput({ label = "Image", value, onChange }: ImageInputProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [showLibrary, setShowLibrary] = useState(false);
  const [library, setLibrary] = useState<string[] | null>(null);

  const handleUpload = async (files: FileList | null) => {
    const file = files?.[0];
    if (!file) return;
    setError(null);
    setProgress(0);
    try {
      const result = await uploadMedia(file, file.name, setProgress);
      onChange(mediaUrl(result.storagePath));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Upload failed");
    } finally {
      setProgress(null);
    }
  };

  const openLibrary = async () => {
    setShowLibrary(true);
    if (library === null) {
      const [uploaded, site] = await Promise.all([
        fetchAdminMedia()
          .then((files) => files.map((m) => mediaUrl(m.storagePath)))
          .catch(() => [] as string[]),
        fetch("/site-images")
          .then((r) => r.json() as Promise<string[]>)
          .catch(() => [] as string[]),
      ]);
      setLibrary(Array.from(new Set([...uploaded, ...site])));
    }
  };

  return (
    <div>
      {label && <Label>{label}</Label>}
      <div className="flex items-start gap-3">
        <div className="relative h-20 w-20 shrink-0 overflow-hidden rounded-[var(--border-radius)] border border-slate-200 bg-slate-50">
          {value ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={value} alt="" className="h-full w-full object-cover" />
          ) : (
            <span className="flex h-full w-full items-center justify-center text-[10px] text-slate-400">
              No image
            </span>
          )}
          {value && (
            <button
              type="button"
              aria-label="Clear image"
              onClick={() => onChange("")}
              className="absolute right-0 top-0 rounded-bl bg-black/50 p-0.5 text-white hover:bg-black/70"
            >
              <X className="h-3 w-3" />
            </button>
          )}
        </div>
        <div className="flex-1">
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            className="hidden"
            onChange={(e) => handleUpload(e.target.files)}
          />
          <div className="flex flex-wrap gap-2">
            <Button type="button" variant="outline" onClick={() => inputRef.current?.click()}>
              <Upload className="h-4 w-4" /> Upload
            </Button>
            <Button type="button" variant="outline" onClick={openLibrary}>
              <Images className="h-4 w-4" /> Choose from library
            </Button>
          </div>
          <Input
            className="mt-2"
            placeholder="…or paste an image URL"
            value={value}
            onChange={(e) => onChange(e.target.value)}
          />
          {progress !== null && (
            <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-200">
              <div
                className="h-full bg-[var(--color-primary)] transition-all"
                style={{ width: `${progress}%` }}
              />
            </div>
          )}
          {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
        </div>
      </div>

      {showLibrary && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="flex max-h-[80vh] w-full max-w-3xl flex-col rounded-[var(--border-radius)] bg-white">
            <div className="flex items-center justify-between border-b border-slate-200 p-4">
              <h3 className="font-semibold">Choose an image</h3>
              <button type="button" aria-label="Close" onClick={() => setShowLibrary(false)}>
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="overflow-y-auto p-4">
              {library === null ? (
                <p className="text-sm text-slate-500">Loading…</p>
              ) : library.length === 0 ? (
                <p className="text-sm text-slate-500">No images found. Upload one instead.</p>
              ) : (
                <ul className="grid grid-cols-3 gap-3 sm:grid-cols-4">
                  {library.map((url) => (
                    <li key={url}>
                      <button
                        type="button"
                        onClick={() => {
                          onChange(url);
                          setShowLibrary(false);
                        }}
                        className={`block w-full overflow-hidden rounded-[var(--border-radius)] border-2 ${
                          value === url ? "border-[var(--color-primary)]" : "border-transparent hover:border-slate-300"
                        }`}
                      >
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img src={url} alt="" className="h-24 w-full bg-slate-50 object-cover" />
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
