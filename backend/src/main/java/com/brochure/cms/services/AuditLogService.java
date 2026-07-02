package com.brochure.cms.services;

import java.util.Map;

/**
 * Records lightweight admin audit log entries for mutating operations.
 */
public interface AuditLogService {

    /**
     * Records an audit log entry for the current tenant and authenticated actor.
     *
     * @param action     the action that occurred (e.g. {@code TAXONOMY_CREATED})
     * @param entityType the type of entity affected (e.g. {@code TAXONOMY})
     * @param entityId   the identifier of the affected entity, may be {@code null}
     * @param details    optional non-sensitive key/value context
     */
    void record(String action, String entityType, String entityId, Map<String, Object> details);
}
