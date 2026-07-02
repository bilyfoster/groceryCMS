package com.brochure.cms.domain.faq;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<FaqItem, UUID> {

    List<FaqItem> findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(
            UUID tenantId, UUID pageId);
}
