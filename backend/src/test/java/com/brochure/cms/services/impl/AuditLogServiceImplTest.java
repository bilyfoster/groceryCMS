package com.brochure.cms.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.models.AuditLog;
import com.brochure.cms.repositories.AuditLogRepository;
import com.brochure.cms.shared.security.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl(auditLogRepository);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void record_When_AuthenticatedUser_Expect_SavedWithTenantAndActor() {
        User user = User.builder()
                .tenantId(TENANT_ID)
                .email("admin@example.com")
                .build();
        user.setId(ACTOR_ID);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.record("TAXONOMY_CREATED", "TAXONOMY", "e1", Map.of("slug", "anxiety"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertEquals(TENANT_ID, saved.getTenantId());
        assertEquals(ACTOR_ID, saved.getActorId());
        assertEquals("TAXONOMY_CREATED", saved.getAction());
        assertEquals("TAXONOMY", saved.getEntityType());
        assertEquals("e1", saved.getEntityId());
        assertEquals("anxiety", saved.getDetails().get("slug"));
    }

    @Test
    void record_When_AnonymousAuthentication_Expect_ActorIdNullAndDetailsNotNull() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.record("TAXONOMY_CREATED", "TAXONOMY", "e1", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertNull(saved.getActorId());
        assertNotNull(saved.getDetails());
        assertEquals("TAXONOMY_CREATED", saved.getAction());
    }

    @Test
    void record_When_NoAuthentication_Expect_ActorIdNull() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.record("THERAPIST_DELETED", "THERAPIST", "e2", Map.of());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertEquals(TENANT_ID, saved.getTenantId());
        assertNull(saved.getActorId());
        assertEquals("THERAPIST", saved.getEntityType());
    }
}
