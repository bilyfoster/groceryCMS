"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { unlockPage, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface PagePasswordGateProps {
  slug: string;
  title: string;
}

export function PagePasswordGate({ slug, title }: PagePasswordGateProps) {
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await unlockPage(slug, password);
      router.refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Incorrect password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 px-4 py-24">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        <p className="mt-2 text-sm text-slate-600">This page is password protected.</p>
      </div>
      <form onSubmit={submit} className="flex flex-col gap-4">
        <div>
          <Label htmlFor="page-password">Password</Label>
          <Input
            id="page-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <Button type="submit" disabled={loading}>
          {loading ? "Checking…" : "View page"}
        </Button>
      </form>
    </div>
  );
}
