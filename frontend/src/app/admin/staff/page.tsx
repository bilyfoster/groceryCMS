"use client";

import { useEffect, useState } from "react";
import {
  createStaffMember,
  deleteStaffMember,
  fetchAdminStaff,
  fetchNavPages,
  updateStaffMember,
} from "@/lib/api";
import type { PageSummary, StaffMember } from "@/types/page";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ImageInput } from "@/components/admin/ImageInput";


interface EditingMember extends Partial<StaffMember> {
  pageId?: string;
}

export default function AdminStaffPage() {
  const [pages, setPages] = useState<PageSummary[]>([]);
  const [selectedPageId, setSelectedPageId] = useState<string>("");
  const [members, setMembers] = useState<StaffMember[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<EditingMember | null>(null);

  useEffect(() => {
    fetchNavPages()
      .catch(() => [])
      .then((pages) => {
        const staffPages = (pages as PageSummary[]).filter((p) => p.pageType === "staff");
        setPages(staffPages);
        if (staffPages.length > 0 && !selectedPageId) {
          setSelectedPageId(staffPages[0].id);
        }
      });
  }, [selectedPageId]);

  useEffect(() => {
    if (!selectedPageId) return;
    setLoading(true);
    fetchAdminStaff(selectedPageId)
      .then(setMembers)
      .catch(() => alert("Failed to load staff members"))
      .finally(() => setLoading(false));
  }, [selectedPageId]);

  const handleSave = async () => {
    if (!editing || !editing.name || !selectedPageId) return;
    try {
      if (editing.id) {
        await updateStaffMember(editing.id, {
          name: editing.name,
          title: editing.title ?? undefined,
          bio: editing.bio ?? undefined,
          photoUrl: editing.photoUrl || undefined,
          email: editing.email || undefined,
          sortOrder: editing.sortOrder,
          published: editing.published,
        });
      } else {
        await createStaffMember({
          pageId: selectedPageId,
          name: editing.name,
          title: editing.title ?? undefined,
          bio: editing.bio ?? undefined,
          photoUrl: editing.photoUrl || undefined,
          email: editing.email || undefined,
          sortOrder: editing.sortOrder ?? 0,
          published: editing.published ?? true,
        });
      }
      setEditing(null);
      const updated = await fetchAdminStaff(selectedPageId);
      setMembers(updated);
    } catch {
      alert("Failed to save staff member");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this staff member?")) return;
    try {
      await deleteStaffMember(id);
      const updated = await fetchAdminStaff(selectedPageId);
      setMembers(updated);
    } catch {
      alert("Failed to delete staff member");
    }
  };

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Staff</h1>
        <Button
          onClick={() =>
            setEditing({
              pageId: selectedPageId,
              name: "",
              title: "",
              bio: "",
              photoUrl: "",
              email: "",
              sortOrder: members.length,
              published: true,
            })
          }
        >
          Add staff member
        </Button>
      </div>

      <div className="mb-6">
        <Label htmlFor="page">Staff page</Label>
        <select
          id="page"
          value={selectedPageId}
          onChange={(e) => setSelectedPageId(e.target.value)}
          className="w-full rounded-[var(--border-radius)] border border-slate-200 bg-white px-3 py-2 text-sm sm:w-80"
        >
          {pages.map((p) => (
            <option key={p.id} value={p.id}>
              {p.title}
            </option>
          ))}
        </select>
      </div>

      {editing && (
        <div className="mb-6 rounded-[var(--border-radius)] border border-slate-200 p-4">
          <h2 className="mb-4 text-lg font-semibold">
            {editing.id ? "Edit staff member" : "New staff member"}
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={editing.name || ""}
                onChange={(e) => setEditing({ ...editing, name: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                value={editing.title || ""}
                onChange={(e) => setEditing({ ...editing, title: e.target.value })}
              />
            </div>
            <ImageInput
              label="Photo"
              value={editing.photoUrl || ""}
              onChange={(url) => setEditing({ ...editing, photoUrl: url })}
            />
            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={editing.email || ""}
                onChange={(e) => setEditing({ ...editing, email: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="sortOrder">Sort order</Label>
              <Input
                id="sortOrder"
                type="number"
                value={editing.sortOrder ?? 0}
                onChange={(e) =>
                  setEditing({ ...editing, sortOrder: parseInt(e.target.value, 10) || 0 })
                }
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                id="published"
                type="checkbox"
                checked={editing.published ?? true}
                onChange={(e) => setEditing({ ...editing, published: e.target.checked })}
              />
              <Label htmlFor="published">Published</Label>
            </div>
            <div className="sm:col-span-2">
              <Label htmlFor="bio">Bio</Label>
              <Textarea
                id="bio"
                rows={4}
                value={editing.bio || ""}
                onChange={(e) => setEditing({ ...editing, bio: e.target.value })}
              />
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <Button onClick={handleSave}>Save</Button>
            <Button variant="outline" onClick={() => setEditing(null)}>
              Cancel
            </Button>
          </div>
        </div>
      )}

      {loading ? (
        <p className="text-slate-600">Loading…</p>
      ) : members.length === 0 ? (
        <p className="text-slate-600">No staff members yet.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {members.map((m) => (
            <li
              key={m.id}
              className="flex items-start justify-between gap-4 rounded-[var(--border-radius)] border border-slate-200 p-4"
            >
              <div>
                <p className="font-semibold">
                  {m.name}
                  {!m.published && (
                    <span className="ml-2 text-xs text-slate-500">(draft)</span>
                  )}

                </p>
                {m.title && <p className="text-sm text-slate-600">{m.title}</p>}
                {m.bio && <p className="mt-1 line-clamp-2 text-sm text-slate-600">{m.bio}</p>}
              </div>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" onClick={() => setEditing(m)}>
                  Edit
                </Button>
                <Button variant="destructive" size="sm" onClick={() => handleDelete(m.id)}>
                  Delete
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
