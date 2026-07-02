package com.brochure.cms.services.impl;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.models.AuditLog;
import com.brochure.cms.repositories.AuditLogRepository;
import com.brochure.cms.services.AuditLogService;
import com.brochure.cms.shared.util.TenantIds;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link AuditLogService} implementation. Captures the current tenant
 * from {@link TenantIds} and the current actor from the Spring Security context.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void record(String action, String entityType, String entityId, Map<String, Object> details) {
        UUID tenantId = TenantIds.current();
        UUID actorId = currentActorId();

        AuditLog entry = AuditLog.builder()
                .tenantId(tenantId)
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details != null ? details : Map.of())
                .build();

        AuditLog saved = auditLogRepository.save(entry);
        log.debug("Recorded audit log {} for action {} on {} {}",
                saved.getId(), action, entityType, entityId);
    }

    private UUID currentActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User u) {
            return u.getId();
        }
        return null;
    }
}
