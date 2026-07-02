package com.brochure.cms.domain.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Tenant> findByDomainAndDeletedAtIsNull(String domain);
}
