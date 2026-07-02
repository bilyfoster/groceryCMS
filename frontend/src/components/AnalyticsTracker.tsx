"use client";

import { useEffect } from "react";
import { trackEvent } from "@/lib/analytics";

interface Props {
  eventType: string;
  metadata?: Record<string, unknown>;
}

export function AnalyticsTracker({ eventType, metadata }: Props) {
  useEffect(() => {
    trackEvent(eventType, metadata);
  }, [eventType, metadata]);
  return null;
}
