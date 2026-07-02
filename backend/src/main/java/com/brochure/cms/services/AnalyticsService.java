package com.brochure.cms.services;

import com.brochure.cms.dto.AnalyticsEventRequestDTO;
import com.brochure.cms.models.AnalyticsEvent;

/**
 * Captures lightweight analytics events scoped to the current tenant.
 */
public interface AnalyticsService {

    /**
     * Records a public analytics event for the current tenant.
     *
     * @param request the event payload
     * @return the persisted analytics event
     */
    AnalyticsEvent recordEvent(AnalyticsEventRequestDTO request);
}
