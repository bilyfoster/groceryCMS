package com.brochure.cms.controllers;

import com.brochure.cms.dto.TaxonomyTermRequestDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.services.AuditLogService;
import com.brochure.cms.services.TaxonomyService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API for managing taxonomy terms. The {@code /api/admin/**} path already
 * requires {@code EDITOR} or {@code ADMIN} (see {@code SecurityConfig}); create
 * and delete are further restricted to {@code ADMIN} (Practice Administrator)
 * per the role rulings in the engineering PRD.
 */
@RestController
@RequestMapping("/api/admin/taxonomies")
@Tag(name = "Taxonomies (admin)")
public class AdminTaxonomyController {

    private final TaxonomyService taxonomyService;
    private final AuditLogService auditLogService;

    public AdminTaxonomyController(TaxonomyService taxonomyService, AuditLogService auditLogService) {
        this.taxonomyService = taxonomyService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List all taxonomy terms of a type (including inactive)")
    public ApiResponse<List<TaxonomyTermResponseDTO>> list(@RequestParam TaxonomyType type) {
        return ApiResponse.ok(taxonomyService.listAll(type));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a taxonomy term (ADMIN only)")
    public ApiResponse<TaxonomyTermResponseDTO> create(@Valid @RequestBody TaxonomyTermRequestDTO request) {
        TaxonomyTermResponseDTO saved = taxonomyService.create(request);
        auditLogService.record("TAXONOMY_CREATED", "TAXONOMY", saved.getId().toString(),
                Map.of("type", saved.getType(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Taxonomy term created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a taxonomy term")
    public ApiResponse<TaxonomyTermResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody TaxonomyTermRequestDTO request) {
        TaxonomyTermResponseDTO saved = taxonomyService.update(id, request);
        auditLogService.record("TAXONOMY_UPDATED", "TAXONOMY", id.toString(),
                Map.of("type", saved.getType(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Taxonomy term updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a taxonomy term (ADMIN only)")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        taxonomyService.delete(id);
        auditLogService.record("TAXONOMY_DELETED", "TAXONOMY", id.toString(), Map.of());
        return ApiResponse.ok(null, "Taxonomy term deleted");
    }
}
