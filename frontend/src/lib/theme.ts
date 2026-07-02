import type { CSSProperties } from "react";
import type { TenantSettings } from "@/types/tenant";

const LOADED_FONT_VARS: Record<string, string> = {
  montserrat: "var(--font-montserrat)",
  barlow: "var(--font-barlow)",
};

function fontVariable(fontName?: string): string | undefined {
  if (!fontName) return undefined;
  const normalized = fontName.trim().toLowerCase();
  return LOADED_FONT_VARS[normalized] ?? fontName;
}

export function tenantCssVariables(
  settings: TenantSettings = {},
): CSSProperties {
  return {
    "--color-primary": settings.primaryColor ?? "#E1A056",
    "--color-secondary": settings.secondaryColor ?? "#8F8F8F",
    "--font-heading": fontVariable(settings.headingFont) ?? "var(--font-montserrat)",
    "--font-body": fontVariable(settings.bodyFont) ?? "var(--font-barlow)",
    "--border-radius": settings.borderRadius ?? "0.5rem",
  } as CSSProperties;
}
