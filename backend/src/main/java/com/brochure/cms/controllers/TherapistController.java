package com.brochure.cms.controllers;

import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSummaryDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.services.TherapistService;
import com.brochure.cms.shared.dto.ApiResponse;
import com.brochure.cms.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public therapist directory and profile API.
 */
@RestController
@RequestMapping("/api/therapists")
@Tag(name = "Therapists (public)")
public class TherapistController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TherapistService therapistService;

    public TherapistController(TherapistService therapistService) {
        this.therapistService = therapistService;
    }

    @GetMapping
    @Operation(summary = "List published therapists with optional filters")
    public ApiResponse<PagedResponse<TherapistSummaryDTO>> directory(
            @RequestParam(required = false) UUID focusArea,
            @RequestParam(required = false) UUID modality,
            @RequestParam(required = false) UUID demographic,
            @RequestParam(required = false) ServiceDelivery delivery,
            @RequestParam(required = false) AvailabilityStatus availability,
            @RequestParam(required = false) String q,
            @Parameter(hidden = true) @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return ApiResponse.ok(therapistService.findPublishedDirectory(
                focusArea, modality, demographic, delivery, availability, q, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a published therapist profile by slug")
    public ApiResponse<TherapistResponseDTO> profile(@PathVariable String slug) {
        return ApiResponse.ok(therapistService.findPublishedBySlug(slug));
    }
}
