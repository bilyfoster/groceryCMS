package com.brochure.cms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public analytics event payload.
 *
 * <p>Captures visitor action metadata only — never clinical data or PHI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Analytics event payload. Only non-clinical metadata may be included.")
public class AnalyticsEventRequestDTO {

    @NotBlank(message = "eventType is required")
    @Schema(description = "Event type, e.g. page_view, profile_view, directory_search", example = "page_view")
    private String eventType;

    @Schema(description = "Arbitrary non-clinical key/value metadata captured with the event")
    private Map<String, Object> metadata;
}
