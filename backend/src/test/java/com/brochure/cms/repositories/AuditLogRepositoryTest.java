package com.brochure.cms.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brochure.cms.models.AuditLog;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogRepositoryTest {

    private static final UUID TENANT_A = UUID.randomUUID();
    private static final UUID TENANT_B = UUID.randomUUID();

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void findByTenantIdAndEntityType_When_MatchingTenant_Expect_Returned() {
        AuditLog entry = saveLog(TENANT_A, "THERAPIST", "THERAPIST_CREATED", "e1");
        saveLog(TENANT_B, "THERAPIST", "THERAPIST_CREATED", "e2");

        List<AuditLog> result = auditLogRepository
                .findByTenantIdAndEntityTypeAndDeletedAtIsNullOrderByCreatedAtDesc(TENANT_A, "THERAPIST");

        assertEquals(1, result.size());
        assertEquals(entry.getId(), result.get(0).getId());
    }

    @Test
    void findByTenantIdAndEntityType_When_DifferentTenant_Expect_Empty() {
        saveLog(TENANT_B, "TAXONOMY", "TAXONOMY_CREATED", "e1");

        List<AuditLog> result = auditLogRepository
                .findByTenantIdAndEntityTypeAndDeletedAtIsNullOrderByCreatedAtDesc(TENANT_A, "TAXONOMY");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByTenantId_When_MultipleEntityTypes_Expect_AllTenantEntries() {
        AuditLog taxonomyLog = saveLog(TENANT_A, "TAXONOMY", "TAXONOMY_CREATED", "t1");
        AuditLog therapistLog = saveLog(TENANT_A, "THERAPIST", "THERAPIST_CREATED", "th1");
        saveLog(TENANT_B, "THERAPIST", "THERAPIST_CREATED", "th2");

        List<AuditLog> result = auditLogRepository.findByTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(TENANT_A);

        assertEquals(2, result.size());
        assertTrue(result.stream().map(AuditLog::getId).toList().containsAll(
                List.of(taxonomyLog.getId(), therapistLog.getId())));
    }

    private AuditLog saveLog(UUID tenantId, String entityType, String action, String entityId) {
        return auditLogRepository.save(AuditLog.builder()
                .tenantId(tenantId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(Map.of("key", "value"))
                .build());
    }
}
