package com.brochure.cms.domain.menu;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    Optional<Menu> findByTenantIdAndLocation(UUID tenantId, String location);
}
