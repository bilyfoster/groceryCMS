"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchRevisions, restoreRevision } from "@/lib/api";
import { Button } from "@/components/ui/button";

interface RevisionPanelProps {
  entityType: "page" | "blog_post";
  entityId: string;
  onRestored?: () => void;
}

export function RevisionPanel({ entityType, entityId, onRestored }: RevisionPanelProps) {
  const queryClient = useQueryClient();
  const { data: revisions = [] } = useQuery({
    queryKey: ["revisions", entityType, entityId],
    queryFn: () => fetchRevisions(entityType, entityId),
  });

  const restoreMutation = useMutation({
    mutationFn: (id: string) => restoreRevision(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["revisions", entityType, entityId] });
      onRestored?.();
    },
  });

  if (revisions.length === 0) {
    return (
      <p className="text-sm text-slate-500">No revisions yet — save to create history.</p>
    );
  }

  return (
    <ul className="space-y-2">
      {revisions.map((r) => (
        <li
          key={r.id}
          className="flex items-center justify-between gap-2 rounded border border-slate-200 px-3 py-2 text-sm"
        >
          <span>{new Date(r.createdAt).toLocaleString()}</span>
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={restoreMutation.isPending}
            onClick={() => restoreMutation.mutate(r.id)}
          >
            Restore
          </Button>
        </li>
      ))}
    </ul>
  );
}
