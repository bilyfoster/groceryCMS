"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  fetchAdminMenu,
  fetchAdminPages,
  saveAdminMenu,
  type AdminMenuItem,
  ApiError,
} from "@/lib/api";
import { StickySaveBar } from "@/components/admin/StickySaveBar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const LOCATIONS = ["header", "footer"] as const;

type DraftItem = {
  label: string;
  url: string;
  pageId: string;
  target: string;
  sortOrder: number;
};

export default function AdminMenusPage() {
  const queryClient = useQueryClient();
  const [location, setLocation] = useState<(typeof LOCATIONS)[number]>("header");
  const [menuName, setMenuName] = useState("");
  const [items, setItems] = useState<DraftItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  const { data: menu } = useQuery({
    queryKey: ["admin-menu", location],
    queryFn: () => fetchAdminMenu(location),
  });
  const { data: pages = [] } = useQuery({
    queryKey: ["admin-pages"],
    queryFn: fetchAdminPages,
  });

  useEffect(() => {
    if (menu) {
      setMenuName(menu.name);
      setItems(
        menu.items.map((i: AdminMenuItem) => ({
          label: i.label,
          url: i.url ?? "",
          pageId: i.pageId ?? "",
          target: i.target,
          sortOrder: i.sortOrder,
        })),
      );
    }
  }, [menu]);

  const saveMutation = useMutation({
    mutationFn: () =>
      saveAdminMenu(location, {
        name: menuName,
        items: items.map((item, index) => ({
          label: item.label,
          url: item.url || null,
          pageId: item.pageId || null,
          target: item.target,
          sortOrder: index,
        })),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-menu", location] });
      setError(null);
    },
    onError: (e) => setError(e instanceof ApiError ? e.message : "Save failed"),
  });

  const addItem = () => {
    setItems((prev) => [
      ...prev,
      { label: "New link", url: "", pageId: "", target: "_self", sortOrder: prev.length },
    ]);
  };

  return (
    <div className="p-4 pb-32 md:p-8">
      <h1 className="mb-6 text-2xl font-bold">Menus</h1>

      <div className="mb-4 flex gap-2">
        {LOCATIONS.map((loc) => (
          <Button
            key={loc}
            type="button"
            variant={location === loc ? "default" : "outline"}
            onClick={() => setLocation(loc)}
          >
            {loc}
          </Button>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{location} menu</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div>
            <Label htmlFor="menuName">Menu name</Label>
            <Input id="menuName" value={menuName} onChange={(e) => setMenuName(e.target.value)} />
          </div>

          {items.map((item, index) => (
            <div
              key={index}
              className="grid gap-2 rounded border border-slate-200 p-3 sm:grid-cols-2"
            >
              <Input
                placeholder="Label"
                value={item.label}
                onChange={(e) => {
                  const next = [...items];
                  next[index] = { ...item, label: e.target.value };
                  setItems(next);
                }}
              />
              <select
                className="rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={item.pageId}
                onChange={(e) => {
                  const next = [...items];
                  next[index] = { ...item, pageId: e.target.value };
                  setItems(next);
                }}
              >
                <option value="">— Page or custom URL —</option>
                {pages.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
              <Input
                placeholder="Custom URL (optional)"
                value={item.url}
                onChange={(e) => {
                  const next = [...items];
                  next[index] = { ...item, url: e.target.value };
                  setItems(next);
                }}
              />
              <Button
                type="button"
                variant="ghost"
                onClick={() => setItems(items.filter((_, i) => i !== index))}
              >
                Remove
              </Button>
            </div>
          ))}

          <Button type="button" variant="outline" onClick={addItem}>
            Add menu item
          </Button>
          {error && <p className="text-sm text-red-600">{error}</p>}
        </CardContent>
      </Card>

      <StickySaveBar
        onSave={() => saveMutation.mutate()}
        saving={saveMutation.isPending}
        label="Save menu"
      />
    </div>
  );
}
