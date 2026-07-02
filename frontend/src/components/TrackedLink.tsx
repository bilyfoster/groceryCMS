"use client";

import Link from "next/link";
import { trackEvent } from "@/lib/analytics";

interface Props {
  href: string;
  eventType: string;
  metadata?: Record<string, unknown>;
  children: React.ReactNode;
  className?: string;
}

export function TrackedLink({ href, eventType, metadata, children, className }: Props) {
  return (
    <Link
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className={className}
      onClick={() => trackEvent(eventType, metadata)}
    >
      {children}
    </Link>
  );
}
