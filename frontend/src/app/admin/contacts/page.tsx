"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchAdminContacts, markContactRead } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function AdminContactsPage() {
  const queryClient = useQueryClient();
  const { data: contacts = [], isLoading, refetch } = useQuery({
    queryKey: ["admin-contacts"],
    queryFn: fetchAdminContacts,
  });

  const readMutation = useMutation({
    mutationFn: (id: string) => markContactRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-contacts"] }),
  });

  return (
    <div className="p-4 md:p-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Contacts</h1>
        <Button variant="outline" onClick={() => refetch()}>
          Refresh
        </Button>
      </div>
      {isLoading ? (
        <p>Loading…</p>
      ) : (
        <ul className="flex flex-col gap-4">
          {contacts.map((c) => (
            <li key={c.id}>
              <Card className={c.readAt ? "opacity-70" : ""}>
                <CardHeader>
                  <CardTitle className="text-base">
                    {c.name} — {c.email}
                  </CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col gap-2 text-sm">
                  {c.subject && <p className="font-medium">{c.subject}</p>}
                  <p>{c.message}</p>
                  {!c.readAt && (
                    <Button size="sm" className="w-fit" onClick={() => readMutation.mutate(c.id)}>
                      Mark read
                    </Button>
                  )}
                </CardContent>
              </Card>
            </li>
          ))}
          {contacts.length === 0 && <p className="text-slate-600">No submissions yet.</p>}
        </ul>
      )}
    </div>
  );
}
