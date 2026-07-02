package com.brochure.cms.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brochure.cms.dto.AnalyticsEventRequestDTO;
import com.brochure.cms.models.AnalyticsEvent;
import com.brochure.cms.services.AnalyticsService;
import com.brochure.cms.shared.security.JwtAuthFilter;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.shared.security.TenantFilter;
import com.brochure.cms.domain.tenant.Tenant;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final String EVENT_TYPE = "profile_view";
    private static final String PATH = "/api/events";
    private static final String MESSAGE = "Event recorded";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenantId = UUID.randomUUID();
        tenant.setId(tenantId);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void recordEvent_When_ValidRequest_Expect_ServiceRecordsEventAndReturnsOk() throws Exception {
        Map<String, Object> metadata = Map.of("page", "/therapists/jane-doe");

        AnalyticsEvent savedEvent = AnalyticsEvent.builder()
                .tenantId(tenantId)
                .eventType(EVENT_TYPE)
                .payload(metadata)
                .build();
        savedEvent.setId(EVENT_ID);

        when(analyticsService.recordEvent(any(AnalyticsEventRequestDTO.class))).thenReturn(savedEvent);

        AnalyticsEventRequestDTO request = AnalyticsEventRequestDTO.builder()
                .eventType(EVENT_TYPE)
                .metadata(metadata)
                .build();

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(MESSAGE))
                .andExpect(jsonPath("$.data.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.data.eventType").value(EVENT_TYPE));

        ArgumentCaptor<AnalyticsEventRequestDTO> captor = ArgumentCaptor.forClass(AnalyticsEventRequestDTO.class);
        verify(analyticsService).recordEvent(captor.capture());

        AnalyticsEventRequestDTO captured = captor.getValue();
        assertThat(captured.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(captured.getMetadata()).containsEntry("page", "/therapists/jane-doe");
    }
}
