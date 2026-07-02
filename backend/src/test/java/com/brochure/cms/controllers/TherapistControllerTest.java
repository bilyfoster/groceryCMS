package com.brochure.cms.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSummaryDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.services.TherapistService;
import com.brochure.cms.shared.dto.PagedResponse;
import com.brochure.cms.shared.security.JwtAuthFilter;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.shared.security.TenantFilter;
import com.brochure.cms.domain.tenant.Tenant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TherapistController.class)
@AutoConfigureMockMvc(addFilters = false)
class TherapistControllerTest {

    private static final UUID THERAPIST_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TherapistService therapistService;

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
    void directory_When_Called_Expect_PagedSummariesReturned() throws Exception {
        TherapistSummaryDTO summary = TherapistSummaryDTO.builder()
                .id(THERAPIST_ID)
                .firstName("Jane")
                .lastName("Doe")
                .slug("jane-doe")
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .focusAreas(List.of())
                .modalities(List.of())
                .demographics(List.of())
                .sortOrder(0)
                .build();
        PagedResponse<TherapistSummaryDTO> page = PagedResponse.of(List.of(summary), 0, 20, 1);

        when(therapistService.findPublishedDirectory(
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/therapists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].slug").value("jane-doe"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void profile_When_SlugExists_Expect_TherapistReturned() throws Exception {
        TherapistResponseDTO response = TherapistResponseDTO.builder()
                .id(THERAPIST_ID)
                .firstName("Jane")
                .lastName("Doe")
                .slug("jane-doe")
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .focusAreas(List.of())
                .modalities(List.of())
                .demographics(List.of())
                .build();

        when(therapistService.findPublishedBySlug("jane-doe")).thenReturn(response);

        mockMvc.perform(get("/api/therapists/jane-doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.slug").value("jane-doe"));
    }
}
