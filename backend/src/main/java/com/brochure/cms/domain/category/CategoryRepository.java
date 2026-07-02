package com.brochure.cms.domain.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId);

    Optional<Category> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Optional<Category> findByTenantIdAndSlugAndDeletedAtIsNull(UUID tenantId, String slug);
}
