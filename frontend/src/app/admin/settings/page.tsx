"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  fetchAdminPages,
  fetchAdminSettings,
  fetchReadingSettings,
  updateAdminSettings,
  updateReadingSettings,
  ApiError,
} from "@/lib/api";
import { StickySaveBar } from "@/components/admin/StickySaveBar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function AdminSettingsPage() {
  const queryClient = useQueryClient();
  const { data: tenant, isLoading } = useQuery({
    queryKey: ["admin-settings"],
    queryFn: fetchAdminSettings,
  });
  const { data: pages = [] } = useQuery({
    queryKey: ["admin-pages"],
    queryFn: fetchAdminPages,
  });
  const { data: reading } = useQuery({
    queryKey: ["reading-settings"],
    queryFn: fetchReadingSettings,
  });

  const [name, setName] = useState("");
  const [tagline, setTagline] = useState("");
  const [logoUrl, setLogoUrl] = useState("");
  const [faviconUrl, setFaviconUrl] = useState("");
  const [analyticsId, setAnalyticsId] = useState("");
  const [customCss, setCustomCss] = useState("");
  const [primaryColor, setPrimaryColor] = useState("#2563eb");
  const [secondaryColor, setSecondaryColor] = useState("#64748b");
  const [frontPageId, setFrontPageId] = useState("");
  const [postsPageId, setPostsPageId] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (tenant) {
      setName(tenant.name);
      const s = tenant.settings;
      if (typeof s.tagline === "string") setTagline(s.tagline);
      if (typeof s.logoUrl === "string") setLogoUrl(s.logoUrl);
      if (typeof s.faviconUrl === "string") setFaviconUrl(s.faviconUrl);
      if (typeof s.analyticsId === "string") setAnalyticsId(s.analyticsId);
      if (typeof s.customCss === "string") setCustomCss(s.customCss);
      if (typeof s.primaryColor === "string") setPrimaryColor(s.primaryColor);
      if (typeof s.secondaryColor === "string") setSecondaryColor(s.secondaryColor);
    }
  }, [tenant]);

  useEffect(() => {
    if (reading) {
      setFrontPageId(reading.frontPageId ?? "");
      setPostsPageId(reading.postsPageId ?? "");
    }
  }, [reading]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      await updateAdminSettings({
        name,
        settings: {
          ...tenant?.settings,
          siteName: name,
          tagline,
          logoUrl: logoUrl || undefined,
          faviconUrl: faviconUrl || undefined,
          analyticsId: analyticsId || undefined,
          customCss: customCss || undefined,
          primaryColor,
          secondaryColor,
        },
      });
      await updateReadingSettings({
        frontPageId: frontPageId || null,
        postsPageId: postsPageId || null,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-settings"] });
      queryClient.invalidateQueries({ queryKey: ["tenant"] });
      queryClient.invalidateQueries({ queryKey: ["reading-settings"] });
      setError(null);
    },
    onError: (e) => setError(e instanceof ApiError ? e.message : "Save failed"),
  });

  if (isLoading) {
    return <div className="p-8">Loading…</div>;
  }

  return (
    <div className="mx-auto max-w-xl p-4 pb-32 md:p-8">
      <h1 className="mb-2 text-2xl font-bold">Site settings</h1>
      <p className="mb-6 text-sm text-slate-500">
        Site identity, reading settings, and theme — WordPress-style configuration.
      </p>

      <div className="flex flex-col gap-6">
        <Card>
          <CardHeader>
            <CardTitle>General</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div>
              <Label htmlFor="siteName">Site name</Label>
              <Input id="siteName" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="tagline">Tagline</Label>
              <Input id="tagline" value={tagline} onChange={(e) => setTagline(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="logoUrl">Logo URL</Label>
              <Input id="logoUrl" value={logoUrl} onChange={(e) => setLogoUrl(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="faviconUrl">Favicon URL</Label>
              <Input id="faviconUrl" value={faviconUrl} onChange={(e) => setFaviconUrl(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="analyticsId">Analytics ID</Label>
              <Input
                id="analyticsId"
                value={analyticsId}
                onChange={(e) => setAnalyticsId(e.target.value)}
                placeholder="G-XXXXXXXX"
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Reading</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div>
              <Label htmlFor="frontPage">Homepage</Label>
              <select
                id="frontPage"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={frontPageId}
                onChange={(e) => setFrontPageId(e.target.value)}
              >
                <option value="">— Select page —</option>
                {pages.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title} ({p.slug})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="postsPage">Posts page</Label>
              <select
                id="postsPage"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={postsPageId}
                onChange={(e) => setPostsPageId(e.target.value)}
              >
                <option value="">— Select page —</option>
                {pages.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title} ({p.slug})
                  </option>
                ))}
              </select>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Theme</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div>
              <Label htmlFor="primaryColor">Primary color</Label>
              <Input
                id="primaryColor"
                type="color"
                value={primaryColor}
                onChange={(e) => setPrimaryColor(e.target.value)}
                className="h-12 w-full cursor-pointer"
              />
            </div>
            <div>
              <Label htmlFor="secondaryColor">Secondary color</Label>
              <Input
                id="secondaryColor"
                type="color"
                value={secondaryColor}
                onChange={(e) => setSecondaryColor(e.target.value)}
                className="h-12 w-full cursor-pointer"
              />
            </div>
            <div>
              <Label htmlFor="customCss">Custom CSS</Label>
              <textarea
                id="customCss"
                className="min-h-[120px] w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-sm"
                value={customCss}
                onChange={(e) => setCustomCss(e.target.value)}
              />
            </div>
          </CardContent>
        </Card>

        {error && <p className="text-sm text-red-600">{error}</p>}
      </div>

      <StickySaveBar
        onSave={() => saveMutation.mutate()}
        saving={saveMutation.isPending}
        label="Save settings"
      />
    </div>
  );
}
