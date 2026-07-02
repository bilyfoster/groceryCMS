package com.brochure.cms.domain.blog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    @Query(
            """
            SELECT b FROM BlogPost b
            WHERE b.tenantId = :tenantId AND b.deletedAt IS NULL AND b.published = true
            ORDER BY b.sticky DESC, b.publishedAt DESC
            """)
    Page<BlogPost> findPublishedOrdered(@Param("tenantId") UUID tenantId, Pageable pageable);

    List<BlogPost> findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID tenantId);

    Optional<BlogPost> findByTenantIdAndSlugAndDeletedAtIsNull(UUID tenantId, String slug);

    Optional<BlogPost> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    List<BlogPost> findByPublishedFalseAndPublishedAtLessThanEqualAndDeletedAtIsNull(OffsetDateTime now);

    @Query(
            """
            SELECT b FROM BlogPost b
            WHERE b.tenantId = :tenantId AND b.deletedAt IS NULL AND b.published = true
              AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(b.slug) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY b.sticky DESC, b.publishedAt DESC
            """)
    List<BlogPost> searchPublished(@Param("tenantId") UUID tenantId, @Param("q") String q);

    @Query(
            """
            SELECT b FROM BlogPost b, com.brochure.cms.domain.category.PostCategory pc
            WHERE pc.postId = b.id AND pc.categoryId = :categoryId AND b.tenantId = :tenantId
              AND b.deletedAt IS NULL AND b.published = true
            ORDER BY b.sticky DESC, b.publishedAt DESC
            """)
    Page<BlogPost> findPublishedByCategory(
            @Param("tenantId") UUID tenantId, @Param("categoryId") UUID categoryId, Pageable pageable);

    @Query(
            value =
                    """
                    SELECT b.* FROM blog_posts b
                    WHERE b.tenant_id = :tenantId AND b.deleted_at IS NULL AND b.published = TRUE
                      AND :tag = ANY(b.tags)
                    ORDER BY b.is_sticky DESC, b.published_at DESC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM blog_posts b
                    WHERE b.tenant_id = :tenantId AND b.deleted_at IS NULL AND b.published = TRUE
                      AND :tag = ANY(b.tags)
                    """,
            nativeQuery = true)
    Page<BlogPost> findPublishedByTag(
            @Param("tenantId") UUID tenantId, @Param("tag") String tag, Pageable pageable);
}
