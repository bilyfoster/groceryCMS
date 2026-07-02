package com.brochure.cms.repositories;

import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link TaxonomyTerm}. All queries are tenant-scoped and
 * exclude soft-deleted rows to enforce multi-tenant isolation.
 */
public interface TaxonomyTermRepository extends JpaRepository<TaxonomyTerm, UUID> {

    List<TaxonomyTerm> findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(
            UUID tenantId, TaxonomyType type);

    List<TaxonomyTerm> findByTenantIdAndTypeAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(
            UUID tenantId, TaxonomyType type);

    Optional<TaxonomyTerm> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    boolean existsByTenantIdAndTypeAndSlugAndDeletedAtIsNull(
            UUID tenantId, TaxonomyType type, String slug);
}
