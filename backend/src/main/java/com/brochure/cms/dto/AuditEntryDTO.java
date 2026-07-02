package com.brochure.cms.dto;

import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single audit log entry returned by admin APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntryDTO {

    private UUID actorId;
    private String action;
    private String entityType;
    private String entityId;
    private Map<String, Object> details;
}
