package com.brochure.cms.domain.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndTenantIdAndDeletedAtIsNull(String email, UUID tenantId);

    Optional<User> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
