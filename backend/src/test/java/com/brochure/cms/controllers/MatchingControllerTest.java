package com.brochure.cms.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.dto.MatchResultDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.services.MatchingService;
import com.brochure.cms.shared.captcha.HcaptchaService;
import com.brochure.cms.shared.security.JwtAuthFilter;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.shared.security.TenantFilter;
import com.brochure.cms.domain.tenant.Tenant;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MatchingController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchingControllerTest {

    private static final UUID THERAPIST_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private HcaptchaService hcaptchaService;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void match_When_ValidRequest_Expect_RankedMatchesReturned() throws Exception {
        doNothing().when(hcaptchaService).verifyIfConfigured(any());

        MatchResultDTO result = MatchResultDTO.builder()
                .therapistId(THERAPIST_ID)
                .slug("jane-doe")
                .firstName("Jane")
                .lastName("Doe")
                .credentials("LPC")
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .score(0.95)
                .rank(1)
                .explanations(List.of("Specializes in Anxiety — 1 of your 1 focus areas"))
                .build();
        when(matchingService.findMatches(any(IntakeRequestDTO.class)))
                .thenReturn(MatchResponseDTO.builder().matches(List.of(result)).build());

        IntakeRequestDTO request = IntakeRequestDTO.builder()
                .areasOfConcern(Set.of(UUID.randomUUID()))
                .preferredDelivery(ServiceDelivery.HYBRID)
                .contactEmail("client@example.com")
                .build();

        mockMvc.perform(post("/api/match")
                        .param("captchaToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matches[0].therapistId").value(THERAPIST_ID.toString()))
                .andExpect(jsonPath("$.data.matches[0].rank").value(1))
                .andExpect(jsonPath("$.data.matches[0].score").value(0.95));
    }
}
