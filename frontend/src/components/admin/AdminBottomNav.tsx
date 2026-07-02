"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FileText, Home, Image, Settings, Users } from "lucide-react";
import { cn } from "@/lib/utils";

const tabs = [
  { href: "/admin", label: "Dashboard", icon: Home },
  { href: "/admin/pages", label: "Content", icon: FileText },
  { href: "/admin/media", label: "Media", icon: Image },
  { href: "/admin/settings", label: "Settings", icon: Settings },
  { href: "/auth/login", label: "Account", icon: Users },
];

export function AdminBottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-slate-200 bg-white pb-[env(safe-area-inset-bottom)] lg:hidden">
      <ul className="flex justify-around">
        {tabs.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || (href !== "/admin" && pathname.startsWith(href));
          return (
            <li key={href} className="flex-1">
              <Link
                href={href}
                className={cn(
                  "flex min-h-[56px] flex-col items-center justify-center gap-0.5 px-1 text-[11px]",
                  active ? "text-[var(--color-primary)]" : "text-slate-600",
                )}
              >
                <Icon className="h-5 w-5" aria-hidden />
                <span className="truncate">{label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
