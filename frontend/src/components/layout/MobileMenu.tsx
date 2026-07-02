"use client";

import { Menu } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Sheet } from "@/components/ui/sheet";
import { Nav } from "@/components/layout/Nav";
import type { PageSummary } from "@/types/page";
import type { MenuItemDto } from "@/types/menu";

interface MobileMenuProps {
  pages: PageSummary[];
  menuItems?: MenuItemDto[];
}

export function MobileMenu({ pages, menuItems = [] }: MobileMenuProps) {
  const [open, setOpen] = useState(false);

  return (
    <div className="md:hidden">
      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label="Open menu"
        onClick={() => setOpen(true)}
      >
        <Menu className="h-6 w-6" />
      </Button>
      <Sheet open={open} onOpenChange={setOpen} title="Menu">
        <Nav pages={pages} menuItems={menuItems} onNavigate={() => setOpen(false)} />
      </Sheet>
    </div>
  );
}
