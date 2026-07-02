package com.brochure.cms.repositories;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.Therapist;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link Therapist}. All queries are tenant-scoped and exclude
 * soft-deleted rows to enforce multi-tenant isolation.
 */
public interface TherapistRepository extends JpaRepository<Therapist, UUID> {

    @Query("""
            select distinct t from Therapist t left join fetch t.terms
            where t.id = :id and t.tenantId = :tenantId and t.deletedAt is null""")
    Optional<Therapist> findByIdAndTenantIdFetchTerms(
            @Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            select distinct t from Therapist t left join fetch t.terms
            where t.slug = :slug and t.tenantId = :tenantId
              and t.published = true and t.deletedAt is null""")
    Optional<Therapist> findPublishedBySlugAndTenantIdFetchTerms(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId);

    @Query("""
            select distinct t from Therapist t left join fetch t.terms
            where t.userId = :userId and t.tenantId = :tenantId and t.deletedAt is null""")
    Optional<Therapist> findByUserIdAndTenantIdFetchTerms(
            @Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    boolean existsBySlugAndTenantIdAndDeletedAtIsNull(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId);

    boolean existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId, @Param("id") UUID id);

    @Query("""
            select distinct t from Therapist t left join fetch t.terms
            where t.tenantId = :tenantId and t.deletedAt is null
            order by t.sortOrder asc, t.lastName asc, t.firstName asc""")
    List<Therapist> findAllByTenantIdFetchTerms(@Param("tenantId") UUID tenantId);

    @Query(value = """
            select distinct t.id from Therapist t
            where t.tenantId = :tenantId
              and t.published = true
              and t.deletedAt is null
              and (:focusAreaId is null or exists (
                  select 1 from t.terms fa where fa.id = :focusAreaId and fa.type = :focusAreaType))
              and (:modalityId is null or exists (
                  select 1 from t.terms mo where mo.id = :modalityId and mo.type = :modalityType))
              and (:demographicId is null or exists (
                  select 1 from t.terms de where de.id = :demographicId and de.type = :demographicType))
              and (:delivery is null or t.serviceDelivery = :delivery)
              and (:availability is null or t.availabilityStatus = :availability)
              and (:searchPattern is null
                   or lower(t.firstName) like :searchPattern
                   or lower(t.lastName) like :searchPattern
                   or lower(t.credentials) like :searchPattern)
            """,
            countQuery = """
                    select count(distinct t.id) from Therapist t
                    where t.tenantId = :tenantId
                      and t.published = true
                      and t.deletedAt is null
                      and (:focusAreaId is null or exists (
                          select 1 from t.terms fa where fa.id = :focusAreaId and fa.type = :focusAreaType))
                      and (:modalityId is null or exists (
                          select 1 from t.terms mo where mo.id = :modalityId and mo.type = :modalityType))
                      and (:demographicId is null or exists (
                          select 1 from t.terms de where de.id = :demographicId and de.type = :demographicType))
                      and (:delivery is null or t.serviceDelivery = :delivery)
                      and (:availability is null or t.availabilityStatus = :availability)
                      and (:searchPattern is null
                           or lower(t.firstName) like :searchPattern
                           or lower(t.lastName) like :searchPattern
                           or lower(t.credentials) like :searchPattern)
                    """)
    Page<UUID> findPublishedDirectoryIds(
            @Param("tenantId") UUID tenantId,
            @Param("focusAreaId") UUID focusAreaId,
            @Param("focusAreaType") TaxonomyType focusAreaType,
            @Param("modalityId") UUID modalityId,
            @Param("modalityType") TaxonomyType modalityType,
            @Param("demographicId") UUID demographicId,
            @Param("demographicType") TaxonomyType demographicType,
            @Param("delivery") ServiceDelivery delivery,
            @Param("availability") AvailabilityStatus availability,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

    @Query("""
            select distinct t from Therapist t left join fetch t.terms
            where t.tenantId = :tenantId and t.id in :ids""")
    List<Therapist> findAllByIdsFetchTerms(
            @Param("tenantId") UUID tenantId, @Param("ids") Collection<UUID> ids);

    @Query("""
            select t from Therapist t
            where t.staffMemberId = :staffMemberId and t.tenantId = :tenantId and t.deletedAt is null""")
    Optional<Therapist> findByStaffMemberIdAndTenantIdAndDeletedAtIsNull(
            @Param("staffMemberId") UUID staffMemberId, @Param("tenantId") UUID tenantId);

    @Query("""
            select t from Therapist t
            where t.staffMemberId in :staffMemberIds and t.tenantId = :tenantId and t.deletedAt is null""")
    List<Therapist> findByStaffMemberIdInAndTenantIdAndDeletedAtIsNull(
            @Param("staffMemberIds") Collection<UUID> staffMemberIds, @Param("tenantId") UUID tenantId);
}
