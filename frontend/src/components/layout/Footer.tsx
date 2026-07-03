import Link from "next/link";
import { Heart, Lock } from "lucide-react";
import { Nav } from "@/components/layout/Nav";
import type { TenantSettingsDto } from "@/types/tenant";
import type { MenuItemDto } from "@/types/menu";

interface FooterProps {
  tenant: TenantSettingsDto;
  menuItems?: MenuItemDto[];
}

export function Footer({ tenant, menuItems = [] }: FooterProps) {
  const social = tenant.settings.socialLinks ?? {};
  const version = process.env.NEXT_PUBLIC_APP_VERSION;

  return (
    <footer className="mt-auto border-t border-slate-200 bg-slate-50">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-8 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap items-center gap-3">
          <p className="text-sm text-slate-600">
            © {new Date().getFullYear()} {tenant.name}. All rights reserved.
          </p>
          {version && (
            <span className="text-xs text-slate-400">v{version}</span>
          )}
          <Link
            href="https://1lpro.com"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex min-h-[44px] items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-[var(--color-primary)]"
          >
            <span>Made with</span>
            <Heart className="h-4 w-4 fill-current" aria-hidden />
            <span>by 1lpro.com</span>
          </Link>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          {menuItems.length > 0 && (
            <Nav menuItems={menuItems} className="flex-row items-center gap-2" />
          )}

          {Object.keys(social).length > 0 && (
            <ul className="flex flex-wrap gap-3">
              {Object.entries(social).map(([key, url]) => (
                <li key={key}>
                  <Link
                    href={url}
                    className="inline-flex min-h-[44px] min-w-[44px] items-center text-sm font-medium text-[var(--color-primary)] hover:underline"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {key}
                  </Link>
                </li>
              ))}
            </ul>
          )}

          <Link
            href="/admin"
            aria-label="Admin"
            title="Admin"
            className="inline-flex min-h-[44px] min-w-[44px] items-center justify-center rounded-full text-[var(--color-primary)] hover:bg-slate-200"
          >
            <Lock className="h-5 w-5" aria-hidden />
          </Link>
        </div>
      </div>
    </footer>
  );
}
