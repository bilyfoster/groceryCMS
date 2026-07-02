"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { requestMagicLink } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const schema = z.object({ email: z.string().email() });

export default function MagicLinkPage() {
  const [sent, setSent] = useState(false);
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async ({ email }: { email: string }) => {
    await requestMagicLink(email);
    setSent(true);
  };

  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <Card>
        <CardHeader>
          <CardTitle>Magic link sign in</CardTitle>
        </CardHeader>
        <CardContent>
          {sent ? (
            <p className="text-sm text-slate-600">
              If an account exists, a login link has been sent to your email.
            </p>
          ) : (
            <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  inputMode="email"
                  autoComplete="email"
                  autoFocus
                  {...register("email")}
                />
              </div>
              <Button type="submit" disabled={isSubmitting}>
                Send link
              </Button>
            </form>
          )}
          <p className="mt-4 text-center text-sm">
            <Link href="/auth/login" className="text-[var(--color-primary)] hover:underline">
              Back to password login
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
