package com.brochure.cms.domain.revision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionRepository extends JpaRepository<Revision, UUID> {

    List<Revision> findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            UUID tenantId, String entityType, UUID entityId);

    Optional<Revision> findByIdAndTenantId(UUID id, UUID tenantId);
}
