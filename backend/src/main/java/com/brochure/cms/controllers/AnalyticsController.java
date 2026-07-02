package com.brochure.cms.controllers;

import com.brochure.cms.dto.AnalyticsEventRequestDTO;
import com.brochure.cms.models.AnalyticsEvent;
import com.brochure.cms.services.AnalyticsService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public analytics event capture endpoint.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Analytics events")
public class AnalyticsController {

    private static final String EVENT_RECORDED_MESSAGE = "Event recorded";

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping
    @Operation(summary = "Record a lightweight analytics event")
    public ApiResponse<AnalyticsEvent> recordEvent(@Valid @RequestBody AnalyticsEventRequestDTO request) {
        AnalyticsEvent event = analyticsService.recordEvent(request);
        return ApiResponse.ok(event, EVENT_RECORDED_MESSAGE);
    }
}
