package com.brochure.cms.repositories;

import com.brochure.cms.models.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link AuditLog}. Queries are tenant-scoped and exclude
 * soft-deleted rows to enforce multi-tenant isolation.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByTenantIdAndEntityTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID tenantId, String entityType);

    List<AuditLog> findByTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID tenantId);
}
