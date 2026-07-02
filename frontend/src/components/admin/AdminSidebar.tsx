"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  ClipboardList,
  FileText,
  Heart,
  HelpCircle,
  Home,
  Image,
  Layers,
  Mail,
  MessageSquare,
  Menu,
  Settings,
  Tag,
  Users,
} from "lucide-react";
import { cn } from "@/lib/utils";

const links = [
  { href: "/admin", label: "Dashboard", icon: Home },
  { href: "/admin/pages", label: "Pages", icon: FileText },
  { href: "/admin/blog", label: "Blog", icon: MessageSquare },
  { href: "/admin/categories", label: "Categories", icon: Tag },
  { href: "/admin/taxonomies", label: "Taxonomies", icon: Layers },
  { href: "/admin/therapists", label: "Therapists", icon: Heart },
  { href: "/admin/intake", label: "Intake", icon: ClipboardList },
  { href: "/admin/menus", label: "Menus", icon: Menu },
  { href: "/admin/staff", label: "Staff", icon: Users },
  { href: "/admin/gallery", label: "Gallery", icon: Image },
  { href: "/admin/faq", label: "FAQ", icon: HelpCircle },
  { href: "/admin/contacts", label: "Contacts", icon: Mail },
  { href: "/admin/media", label: "Media", icon: Image },
  { href: "/admin/settings", label: "Settings", icon: Settings },
];

export function AdminSidebar() {
  const pathname = usePathname();

  return (
    <aside className="hidden w-56 shrink-0 border-r border-slate-200 bg-slate-50 lg:block">
      <nav className="flex flex-col gap-1 p-4">
        {links.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || (href !== "/admin" && pathname.startsWith(href));
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "inline-flex min-h-[44px] items-center gap-2 rounded-[var(--border-radius)] px-3 py-2 text-sm font-medium",
                active
                  ? "bg-[var(--color-primary)] text-white"
                  : "text-slate-700 hover:bg-slate-200",
              )}
            >
              <Icon className="h-4 w-4" aria-hidden />
              {label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
