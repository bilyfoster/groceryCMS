package com.brochure.cms.domain.staff;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<StaffMember, UUID> {

    List<StaffMember> findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(
            UUID tenantId, UUID pageId);

    List<StaffMember> findByTenantIdAndPageIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID tenantId, UUID pageId);

    java.util.Optional<StaffMember> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    @Query("""
            select s from StaffMember s
            where s.tenantId = :tenantId and s.deletedAt is null
            order by s.sortOrder asc, s.name asc""")
    List<StaffMember> findAllByTenantIdAndDeletedAtIsNullOrderBySortOrderAsc(@Param("tenantId") UUID tenantId);
}
