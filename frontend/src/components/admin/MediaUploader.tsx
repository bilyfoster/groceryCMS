"use client";

import { useRef, useState } from "react";
import { Upload } from "lucide-react";
import { uploadMedia } from "@/lib/api";
import { Button } from "@/components/ui/button";

interface MediaUploaderProps {
  onUploaded?: (url: string) => void;
}

export function MediaUploader({ onUploaded }: MediaUploaderProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleUpload = async (files: FileList | null) => {
    if (!files?.length) return;
    setError(null);
    for (const file of Array.from(files)) {
      try {
        setProgress(0);
        const result = await uploadMedia(file, file.name, setProgress);
        onUploaded?.(result.storagePath);
      } catch (e) {
        setError(e instanceof Error ? e.message : "Upload failed");
      } finally {
        setProgress(null);
      }
    }
  };

  return (
    <div className="flex flex-col gap-2">
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        capture="environment"
        multiple
        className="hidden"
        onChange={(e) => handleUpload(e.target.files)}
      />
      <Button
        type="button"
        variant="outline"
        onClick={() => inputRef.current?.click()}
      >
        <Upload className="h-4 w-4" />
        Upload image
      </Button>
      {progress !== null && (
        <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
          <div
            className="h-full bg-[var(--color-primary)] transition-all"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  );
}
