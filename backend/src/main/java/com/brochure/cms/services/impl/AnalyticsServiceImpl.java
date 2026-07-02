package com.brochure.cms.services.impl;

import com.brochure.cms.dto.AnalyticsEventRequestDTO;
import com.brochure.cms.models.AnalyticsEvent;
import com.brochure.cms.repositories.AnalyticsEventRepository;
import com.brochure.cms.services.AnalyticsService;
import com.brochure.cms.shared.util.TenantIds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link AnalyticsService} implementation. Persists tenant-scoped
 * analytics events without capturing clinical information.
 */
@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final AnalyticsEventRepository analyticsEventRepository;

    public AnalyticsServiceImpl(AnalyticsEventRepository analyticsEventRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
    }

    @Override
    public AnalyticsEvent recordEvent(AnalyticsEventRequestDTO request) {
        UUID tenantId = TenantIds.current();

        AnalyticsEvent event = AnalyticsEvent.builder()
                .tenantId(tenantId)
                .eventType(request.getEventType())
                .payload(toPayload(request.getMetadata()))
                .build();

        AnalyticsEvent saved = analyticsEventRepository.save(event);
        log.info("Recorded analytics event {} ({}) for tenant {}",
                saved.getId(), saved.getEventType(), tenantId);
        return saved;
    }

    private Map<String, Object> toPayload(Map<String, Object> metadata) {
        return metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
}
