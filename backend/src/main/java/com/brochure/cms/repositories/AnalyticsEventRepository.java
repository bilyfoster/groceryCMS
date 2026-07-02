package com.brochure.cms.repositories;

import com.brochure.cms.models.AnalyticsEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link AnalyticsEvent}. All queries are tenant-scoped to
 * enforce multi-tenant isolation.
 */
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    @Query("""
            select e from AnalyticsEvent e
            where e.tenantId = :tenantId
            order by e.createdAt desc""")
    List<AnalyticsEvent> findByTenantIdOrderByCreatedAtDesc(
            @Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("""
            select e from AnalyticsEvent e
            where e.tenantId = :tenantId and e.eventType = :eventType
            order by e.createdAt desc""")
    List<AnalyticsEvent> findByTenantIdAndEventTypeOrderByCreatedAtDesc(
            @Param("tenantId") UUID tenantId, @Param("eventType") String eventType, Pageable pageable);
}
