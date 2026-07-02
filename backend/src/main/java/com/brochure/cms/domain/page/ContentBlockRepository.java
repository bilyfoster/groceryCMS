package com.brochure.cms.domain.page;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    List<ContentBlock> findByPageIdOrderBySortOrderAsc(UUID pageId);

    java.util.Optional<ContentBlock> findByIdAndPageId(UUID id, UUID pageId);
}
