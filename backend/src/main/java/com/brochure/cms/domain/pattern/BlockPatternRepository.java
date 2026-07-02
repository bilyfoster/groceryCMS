package com.brochure.cms.domain.pattern;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockPatternRepository extends JpaRepository<BlockPattern, UUID> {

    @Query(
            """
            SELECT p FROM BlockPattern p
            WHERE p.deletedAt IS NULL
              AND (p.system = true OR p.tenantId = :tenantId)
            ORDER BY p.category ASC, p.name ASC
            """)
    List<BlockPattern> findAvailableForTenant(@Param("tenantId") UUID tenantId);

    Optional<BlockPattern> findByIdAndTenantIdAndDeletedAtIsNullAndSystemFalse(UUID id, UUID tenantId);
}
