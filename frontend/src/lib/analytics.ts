import { recordEvent } from "./api";

export function trackEvent(eventType: string, metadata?: Record<string, unknown>) {
  // Fire-and-forget; analytics must never block the UI.
  recordEvent(eventType, metadata).catch(() => {
    // Silently ignore analytics failures.
  });
}
