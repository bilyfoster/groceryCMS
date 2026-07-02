import Link from "next/link";
import type { PageSummary } from "@/types/page";
import type { MenuItemDto } from "@/types/menu";
import { cn } from "@/lib/utils";

interface NavProps {
  pages?: PageSummary[];
  menuItems?: MenuItemDto[];
  className?: string;
  onNavigate?: () => void;
}

function NavLink({
  href,
  label,
  target,
  onNavigate,
}: {
  href: string;
  label: string;
  target?: string;
  onNavigate?: () => void;
}) {
  const external = target === "_blank" || href.startsWith("http");
  const className =
    "inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 hover:text-[var(--color-primary)]";
  if (external) {
    return (
      <a href={href} target={target} rel="noopener noreferrer" onClick={onNavigate} className={className}>
        {label}
      </a>
    );
  }
  return (
    <Link href={href} onClick={onNavigate} className={className}>
      {label}
    </Link>
  );
}

function MenuTree({ items, onNavigate }: { items: MenuItemDto[]; onNavigate?: () => void }) {
  return (
    <>
      {items.map((item) => (
        <NavLink
          key={item.id}
          href={item.href}
          label={item.label}
          target={item.target}
          onNavigate={onNavigate}
        />
      ))}
    </>
  );
}

export function Nav({ pages = [], menuItems = [], className, onNavigate }: NavProps) {
  const useMenu = menuItems.length > 0;

  return (
    <nav className={cn("flex flex-col gap-1 md:flex-row md:items-center md:gap-4", className)}>
      {useMenu ? (
        <MenuTree items={menuItems} onNavigate={onNavigate} />
      ) : (
        pages.map((page) => (
          <NavLink
            key={page.id}
            href={page.frontPage || page.slug === "home" ? "/" : `/${page.slug}`}
            label={page.title}
            onNavigate={onNavigate}
          />
        ))
      )}
    </nav>
  );
}
