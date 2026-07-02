"use client";

import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { verifyMagicLink } from "@/lib/auth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

function VerifyContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      setError("Missing verification token");
      return;
    }
    verifyMagicLink(token)
      .then(() => {
        router.push("/admin");
        router.refresh();
      })
      .catch(() => setError("Invalid or expired link"));
  }, [searchParams, router]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Verifying…</CardTitle>
      </CardHeader>
      <CardContent>
        {error ? (
          <>
            <p className="text-sm text-red-600">{error}</p>
            <Link href="/auth/login" className="mt-4 inline-flex min-h-[44px] items-center text-[var(--color-primary)]">
              Back to login
            </Link>
          </>
        ) : (
          <p className="text-sm text-slate-600">Please wait while we sign you in.</p>
        )}
      </CardContent>
    </Card>
  );
}

export default function VerifyPage() {
  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <Suspense fallback={<p className="text-center">Loading…</p>}>
        <VerifyContent />
      </Suspense>
    </div>
  );
}
