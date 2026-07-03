import Link from "next/link";
import Image from "next/image";
import { Rss } from "lucide-react";
import { Nav } from "@/components/layout/Nav";
import { MobileMenu } from "@/components/layout/MobileMenu";
import type { PageSummary } from "@/types/page";
import type { MenuItemDto } from "@/types/menu";
import type { TenantSettingsDto } from "@/types/tenant";

interface HeaderProps {
  tenant: TenantSettingsDto;
  pages: PageSummary[];
  menuItems?: MenuItemDto[];
}

function BrandMark() {
  return (
    <span
      aria-hidden="true"
      className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[var(--color-primary)] text-sm font-black leading-none text-white shadow-sm"
      style={{ fontFamily: "var(--font-heading)" }}
    >
      PB
    </span>
  );
}

export function Header({ tenant, pages, menuItems = [] }: HeaderProps) {
  const logoUrl = tenant.settings.logoUrl as string | undefined;
  const siteName = (tenant.settings.siteName as string | undefined) || tenant.name;

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
        <Link
          href="/"
          className="inline-flex min-h-[44px] items-center gap-2 font-semibold text-[var(--color-primary)]"
          style={{ fontFamily: "var(--font-heading)" }}
        >
          {logoUrl ? (
            <Image src={logoUrl} alt="" width={36} height={36} className="h-9 w-auto rounded-lg" />
          ) : (
            <BrandMark />
          )}
          <span>{siteName}</span>
        </Link>
        <div className="hidden items-center gap-4 md:flex">
          <Nav pages={pages} menuItems={menuItems} />
          <a
            href="/rss.xml"
            aria-label="Subscribe to the blog RSS feed"
            title="RSS feed"
            className="inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-500 hover:bg-slate-100 hover:text-[var(--color-primary)]"
          >
            <Rss className="h-5 w-5" />
          </a>
        </div>
        <MobileMenu pages={pages} menuItems={menuItems} />
      </div>
    </header>
  );
}
