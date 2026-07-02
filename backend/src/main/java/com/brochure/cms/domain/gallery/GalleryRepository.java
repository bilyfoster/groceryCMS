package com.brochure.cms.domain.gallery;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryRepository extends JpaRepository<GalleryImage, UUID> {

    List<GalleryImage> findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(
            UUID tenantId, UUID pageId);
}
