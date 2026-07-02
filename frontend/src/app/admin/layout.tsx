import { redirect } from "next/navigation";
import { fetchCurrentUser } from "@/lib/api";
import { isEditorOrAdmin } from "@/lib/auth";
import { AdminBottomNav } from "@/components/admin/AdminBottomNav";
import { AdminSidebar } from "@/components/admin/AdminSidebar";

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = await fetchCurrentUser();
  if (!user || !isEditorOrAdmin(user.role)) {
    redirect("/auth/login");
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)] pb-[calc(5rem+env(safe-area-inset-bottom))] lg:pb-0">
      <AdminSidebar />
      <div className="flex-1 overflow-auto">{children}</div>
      <AdminBottomNav />
    </div>
  );
}
