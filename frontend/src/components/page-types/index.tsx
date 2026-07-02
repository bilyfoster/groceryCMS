import type { ComponentType } from "react";
import type { PageDetail, PageType } from "@/types/page";
import { HomePage } from "@/components/page-types/HomePage";
import { BlogPage } from "@/components/page-types/BlogPage";
import { FaqPage } from "@/components/page-types/FaqPage";
import { StaffPage } from "@/components/page-types/StaffPage";
import { GalleryPage } from "@/components/page-types/GalleryPage";
import { ContactPage } from "@/components/page-types/ContactPage";
import { CustomPage } from "@/components/page-types/CustomPage";
import { ServicePage } from "@/components/page-types/ServicePage";

export interface PageTypeProps {
  page: PageDetail;
}

export const PAGE_TYPE_COMPONENTS: Record<
  PageType,
  ComponentType<PageTypeProps>
> = {
  home: HomePage,
  blog: BlogPage,
  faq: FaqPage,
  staff: StaffPage,
  gallery: GalleryPage,
  contact: ContactPage,
  service: ServicePage,
  custom: CustomPage,
  "404": CustomPage,
};
