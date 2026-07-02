"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { submitContact } from "@/lib/api";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ApiError } from "@/lib/api";

const schema = z.object({
  name: z.string().min(1, "Name is required"),
  email: z.string().email("Valid email required"),
  phone: z.string().optional(),
  subject: z.string().optional(),
  message: z.string().min(1, "Message is required"),
});

type FormData = z.infer<typeof schema>;

export function ContactPage({ page }: PageTypeProps) {
  const layout = page.layout || "centered";
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setError(null);
    try {
      await submitContact(data);
      setSuccess(true);
      reset();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Submission failed");
    }
  };

  const form = (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <div>
        <Label htmlFor="name">Name</Label>
        <Input id="name" autoFocus autoComplete="name" {...register("name")} />
        {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
      </div>
      <div>
        <Label htmlFor="email">Email</Label>
        <Input
          id="email"
          type="email"
          inputMode="email"
          autoComplete="email"
          {...register("email")}
        />
        {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
      </div>
      <div>
        <Label htmlFor="phone">Phone</Label>
        <Input id="phone" type="tel" inputMode="tel" autoComplete="tel" {...register("phone")} />
      </div>
      <div>
        <Label htmlFor="subject">Subject</Label>
        <Input id="subject" {...register("subject")} />
      </div>
      <div>
        <Label htmlFor="message">Message</Label>
        <Textarea id="message" rows={5} {...register("message")} />
        {errors.message && <p className="text-sm text-red-600">{errors.message.message}</p>}
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      {success && <p className="text-sm text-green-700">Thank you! We will be in touch soon.</p>}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Sending…" : "Send message"}
      </Button>
    </form>
  );

  return (
    <article
      className={
        layout === "split"
          ? "mx-auto grid max-w-6xl gap-8 px-4 py-8 md:grid-cols-2"
          : layout === "sidebar"
            ? "mx-auto grid max-w-6xl gap-8 px-4 py-8 md:grid-cols-[1fr_320px]"
            : "mx-auto max-w-lg px-4 py-8"
      }
    >
      <div>
        <BlockRenderer blocks={page.blocks} />
        <h1 className="mb-6 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
          {page.title}
        </h1>
        {layout !== "sidebar" && form}
      </div>
      {layout === "sidebar" && <aside>{form}</aside>}
      {layout === "split" && <div>{form}</div>}
    </article>
  );
}
