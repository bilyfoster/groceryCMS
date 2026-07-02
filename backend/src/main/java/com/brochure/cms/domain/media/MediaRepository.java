package com.brochure.cms.domain.media;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<MediaFile, UUID> {

    List<MediaFile> findByTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID tenantId);

    Optional<MediaFile> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
