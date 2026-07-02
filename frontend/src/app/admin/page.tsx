import Link from "next/link";
import {
  fetchAdminContacts,
  fetchAdminMedia,
  fetchBlogPosts,
  fetchNavPages,
} from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default async function AdminDashboardPage() {
  const [pages, posts, contacts, media] = await Promise.all([
    fetchNavPages().catch(() => []),
    fetchBlogPosts(0, 1).catch(() => ({ totalElements: 0, items: [] })),
    fetchAdminContacts().catch(() => []),
    fetchAdminMedia().catch(() => []),
  ]);

  const stats = [
    { label: "Published pages", value: pages.length, href: "/admin/pages" },
    { label: "Blog posts", value: posts.totalElements, href: "/admin/blog" },
    { label: "Contacts", value: contacts.length, href: "/admin/contacts" },
    { label: "Media files", value: media.length, href: "/admin/media" },
  ];

  return (
    <div className="p-4 md:p-8">
      <h1 className="mb-6 text-2xl font-bold">Dashboard</h1>
      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <li key={stat.label}>
            <Link href={stat.href}>
              <Card className="transition-shadow hover:shadow-md">
                <CardHeader>
                  <CardTitle className="text-base font-medium text-slate-600">
                    {stat.label}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-bold text-[var(--color-primary)]">
                    {stat.value}
                  </p>
                </CardContent>
              </Card>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
