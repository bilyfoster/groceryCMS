package com.brochure.cms.domain.contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<ContactSubmission, UUID> {

    List<ContactSubmission> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<ContactSubmission> findByIdAndTenantId(UUID id, UUID tenantId);
}
