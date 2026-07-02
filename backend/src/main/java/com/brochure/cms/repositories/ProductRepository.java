package com.brochure.cms.repositories;

import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.Product;
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
 * Data access for {@link Product}. All queries are tenant-scoped and exclude
 * soft-deleted rows to enforce multi-tenant isolation.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("""
            select distinct p from Product p left join fetch p.terms
            where p.id = :id and p.tenantId = :tenantId and p.deletedAt is null""")
    Optional<Product> findByIdAndTenantIdFetchTerms(
            @Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            select distinct p from Product p left join fetch p.terms
            where p.slug = :slug and p.tenantId = :tenantId
              and p.published = true and p.deletedAt is null""")
    Optional<Product> findPublishedBySlugAndTenantIdFetchTerms(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId);

    boolean existsBySlugAndTenantIdAndDeletedAtIsNull(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId);

    boolean existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(
            @Param("slug") String slug, @Param("tenantId") UUID tenantId, @Param("id") UUID id);

    @Query("""
            select distinct p from Product p left join fetch p.terms
            where p.tenantId = :tenantId and p.deletedAt is null
            order by p.sortOrder asc, p.name asc""")
    List<Product> findAllByTenantIdFetchTerms(@Param("tenantId") UUID tenantId);

    @Query(value = """
            select distinct p.id from Product p
            where p.tenantId = :tenantId
              and p.published = true
              and p.deletedAt is null
              and (:allergyTypeId is null or exists (
                  select 1 from p.terms at where at.id = :allergyTypeId and at.type = :allergyTypeType))
              and (:dietTypeId is null or exists (
                  select 1 from p.terms dt where dt.id = :dietTypeId and dt.type = :dietTypeType))
              and (:categoryId is null or exists (
                  select 1 from p.terms cat where cat.id = :categoryId and cat.type = :categoryType))
              and (:storeSection is null or p.storeSection = :storeSection)
              and (:stockStatus is null or p.stockStatus = :stockStatus)
              and (:searchPattern is null
                   or lower(p.name) like :searchPattern
                   or lower(coalesce(p.brand, '')) like :searchPattern
                   or lower(p.description) like :searchPattern)
            """,
            countQuery = """
                    select count(distinct p.id) from Product p
                    where p.tenantId = :tenantId
                      and p.published = true
                      and p.deletedAt is null
                      and (:allergyTypeId is null or exists (
                          select 1 from p.terms at where at.id = :allergyTypeId and at.type = :allergyTypeType))
                      and (:dietTypeId is null or exists (
                          select 1 from p.terms dt where dt.id = :dietTypeId and dt.type = :dietTypeType))
                      and (:categoryId is null or exists (
                          select 1 from p.terms cat where cat.id = :categoryId and cat.type = :categoryType))
                      and (:storeSection is null or p.storeSection = :storeSection)
                      and (:stockStatus is null or p.stockStatus = :stockStatus)
                      and (:searchPattern is null
                           or lower(p.name) like :searchPattern
                           or lower(coalesce(p.brand, '')) like :searchPattern
                           or lower(p.description) like :searchPattern)
                    """)
    Page<UUID> findPublishedDirectoryIds(
            @Param("tenantId") UUID tenantId,
            @Param("allergyTypeId") UUID allergyTypeId,
            @Param("allergyTypeType") TaxonomyType allergyTypeType,
            @Param("dietTypeId") UUID dietTypeId,
            @Param("dietTypeType") TaxonomyType dietTypeType,
            @Param("categoryId") UUID categoryId,
            @Param("categoryType") TaxonomyType categoryType,
            @Param("storeSection") StoreSection storeSection,
            @Param("stockStatus") StockStatus stockStatus,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

    @Query("""
            select distinct p from Product p left join fetch p.terms
            where p.tenantId = :tenantId and p.id in :ids""")
    List<Product> findAllByIdsFetchTerms(
            @Param("tenantId") UUID tenantId, @Param("ids") Collection<UUID> ids);
}
