package com.brochure.cms.controllers;

import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.services.TaxonomyService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public read API for taxonomy terms — used by directory filters and the
 * intake questionnaire. Returns only active terms.
 */
@RestController
@RequestMapping("/api/taxonomies")
@Tag(name = "Taxonomies (public)")
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @GetMapping
    @Operation(summary = "List active taxonomy terms of a given type")
    public ApiResponse<List<TaxonomyTermResponseDTO>> list(@RequestParam TaxonomyType type) {
        return ApiResponse.ok(taxonomyService.listActive(type));
    }
}
