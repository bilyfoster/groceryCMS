import type { Metadata, Viewport } from "next";

export const dynamic = "force-dynamic";
import { Montserrat, Barlow } from "next/font/google";
import "./globals.css";
import { fetchMenu, fetchNavPages, fetchTenantSettings } from "@/lib/api";
import { tenantCssVariables } from "@/lib/theme";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { TenantHead } from "@/components/layout/TenantHead";
import { QueryProvider } from "@/components/providers/QueryProvider";
import type { MenuItemDto } from "@/types/menu";

const montserrat = Montserrat({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-montserrat",
  weight: ["400", "500", "600", "700"],
});

const barlow = Barlow({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-barlow",
  weight: ["400", "500", "600"],
});

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

export const metadata: Metadata = {
  title: { default: "BrochureCMS", template: "%s | BrochureCMS" },
  description: "Multi-tenant brochure CMS",
  icons: {
    icon: "/favicon.png",
    apple: "/favicon.png",
  },
};

export default async function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  const [tenant, pages, headerMenuItems, footerMenuItems] = await Promise.all([
    fetchTenantSettings().catch(() => ({
      slug: "demo",
      name: "BrochureCMS",
      settings: {},
    })),
    fetchNavPages().catch(() => []),
    fetchMenu("header").catch(() => [] as MenuItemDto[]),
    fetchMenu("footer").catch(() => [] as MenuItemDto[]),
  ]);

  const cssVars = tenantCssVariables(tenant.settings);

  return (
    <html
      lang="en"
      className={`${montserrat.variable} ${barlow.variable}`}
    >
      <head>
        <TenantHead tenant={tenant} />
      </head>
      <body style={cssVars} className="flex min-h-screen flex-col antialiased">
        <QueryProvider>
          <Header tenant={tenant} pages={pages} menuItems={headerMenuItems} />
          <main className="flex-1">{children}</main>
          <Footer tenant={tenant} menuItems={footerMenuItems} />
        </QueryProvider>
      </body>
    </html>
  );
}
