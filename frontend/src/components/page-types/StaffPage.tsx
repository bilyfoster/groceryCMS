import Image from "next/image";
import { fetchStaff } from "@/lib/api";
import { BlockRenderer } from "@/components/blocks/BlockRenderer";
import type { PageTypeProps } from "@/components/page-types";
import type { StaffMember } from "@/types/page";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export async function StaffPage({ page }: PageTypeProps) {
  const layout = page.layout || "grid";
  const members = await fetchStaff(page.id);

  const Layout =
    layout === "list"
      ? StaffListLayout
      : layout === "cards"
        ? StaffCardsLayout
        : StaffGridLayout;

  return (
    <article className="mx-auto max-w-6xl px-4 py-8">
      <BlockRenderer blocks={page.blocks} />
      <h1 className="mb-6 text-3xl font-bold" style={{ fontFamily: "var(--font-heading)" }}>
        {page.title}
      </h1>
      <Layout members={members} />
    </article>
  );
}

function StaffGridLayout({ members }: { members: StaffMember[] }) {
  return (
    <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {members.map((m) => (
        <StaffCard key={m.id} member={m} />
      ))}
    </ul>
  );
}

function StaffCardsLayout({ members }: { members: StaffMember[] }) {
  return (
    <ul className="flex flex-col gap-6">
      {members.map((m) => (
        <li key={m.id}>
          <Card className="overflow-hidden">
            <div className="flex flex-col md:flex-row">
              {m.photoUrl && (
                <div className="relative h-48 w-full md:h-auto md:w-48 shrink-0">
                  <Image src={m.photoUrl} alt={m.name} fill className="object-cover" sizes="192px" />
                </div>
              )}
              <div className="flex-1 p-4">
                <CardHeader className="p-0">
                  <CardTitle>{m.name}</CardTitle>
                  {m.title && <p className="text-sm text-[var(--color-secondary)]">{m.title}</p>}
                </CardHeader>
                {m.bio && <CardContent className="p-0 pt-2"><p className="text-sm text-slate-600">{m.bio}</p></CardContent>}
              </div>
            </div>
          </Card>
        </li>
      ))}
    </ul>
  );
}

function StaffListLayout({ members }: { members: StaffMember[] }) {
  return (
    <ul className="flex flex-col divide-y divide-slate-200">
      {members.map((m) => (
        <li key={m.id} className="flex gap-4 py-4">
          {m.photoUrl && (
            <Image src={m.photoUrl} alt={m.name} width={64} height={64} className="rounded-full object-cover" />
          )}
          <div>
            <h3 className="font-semibold">{m.name}</h3>
            {m.title && <p className="text-sm text-slate-500">{m.title}</p>}
            {m.bio && <p className="mt-1 text-sm text-slate-600">{m.bio}</p>}
          </div>
        </li>
      ))}
    </ul>
  );
}

function StaffCard({ member }: { member: StaffMember }) {
  return (
    <li>
      <div className="flex h-full flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white transition-shadow hover:shadow-md">
        {member.photoUrl && (
          <div className="relative aspect-[4/5] w-full bg-slate-100">
            <Image
              src={member.photoUrl}
              alt={member.name}
              fill
              className="object-cover"
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 360px"
            />
          </div>
        )}
        <div className="flex flex-1 flex-col p-5">
          <h3 className="text-lg font-semibold text-slate-900">{member.name}</h3>
          {member.title && (
            <p className="mt-0.5 text-sm font-medium text-[var(--color-primary)]">{member.title}</p>
          )}
          {member.bio && (
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{member.bio}</p>
          )}
        </div>
      </div>
    </li>
  );
}
