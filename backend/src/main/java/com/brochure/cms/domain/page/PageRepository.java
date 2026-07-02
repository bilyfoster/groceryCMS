package com.brochure.cms.domain.page;

import com.brochure.cms.enums.PageType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageRepository extends JpaRepository<Page, UUID> {

    List<Page> findByTenantIdAndDeletedAtIsNullAndPublishedTrueOrderByNavOrderAsc(UUID tenantId);

    List<Page> findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID tenantId);

    Optional<Page> findByTenantIdAndSlugAndDeletedAtIsNull(UUID tenantId, String slug);

    Optional<Page> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Optional<Page> findByTenantIdAndFrontPageTrueAndDeletedAtIsNullAndPublishedTrue(UUID tenantId);

    Optional<Page> findByTenantIdAndPostsPageTrueAndDeletedAtIsNullAndPublishedTrue(UUID tenantId);

    Optional<Page> findByTenantIdAndPageTypeAndDeletedAtIsNullAndPublishedTrue(UUID tenantId, PageType pageType);

    List<Page> findByTenantIdAndFrontPageTrueAndDeletedAtIsNull(UUID tenantId);

    List<Page> findByTenantIdAndPostsPageTrueAndDeletedAtIsNull(UUID tenantId);

    @Query(
            """
            SELECT p FROM Page p
            WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL AND p.published = true
              AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY p.title ASC
            """)
    List<Page> searchPublished(@Param("tenantId") UUID tenantId, @Param("q") String q);
}
