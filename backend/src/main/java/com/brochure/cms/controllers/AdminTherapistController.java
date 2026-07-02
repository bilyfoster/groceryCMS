package com.brochure.cms.controllers;

import com.brochure.cms.dto.TherapistRequestDTO;
import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.services.AuditLogService;
import com.brochure.cms.services.TherapistService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API for managing therapist profiles. The {@code /api/admin/**} path
 * requires {@code EDITOR} or {@code ADMIN}; this controller reinforces that on
 * every endpoint.
 */
@RestController
@RequestMapping("/api/admin/therapists")
@PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
@Tag(name = "Therapists (admin)")
public class AdminTherapistController {

    private final TherapistService therapistService;
    private final AuditLogService auditLogService;

    public AdminTherapistController(TherapistService therapistService, AuditLogService auditLogService) {
        this.therapistService = therapistService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List all therapist profiles for the current tenant")
    public ApiResponse<List<TherapistResponseDTO>> list() {
        return ApiResponse.ok(therapistService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a therapist profile by id")
    public ApiResponse<TherapistResponseDTO> get(@PathVariable UUID id) {
        return ApiResponse.ok(therapistService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a therapist profile")
    public ApiResponse<TherapistResponseDTO> create(@Valid @RequestBody TherapistRequestDTO request) {
        TherapistResponseDTO saved = therapistService.create(request);
        auditLogService.record("THERAPIST_CREATED", "THERAPIST", saved.getId().toString(),
                Map.of("firstName", saved.getFirstName(), "lastName", saved.getLastName(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Therapist created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a therapist profile")
    public ApiResponse<TherapistResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody TherapistRequestDTO request) {
        TherapistResponseDTO saved = therapistService.update(id, request);
        auditLogService.record("THERAPIST_UPDATED", "THERAPIST", id.toString(),
                Map.of("firstName", saved.getFirstName(), "lastName", saved.getLastName(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Therapist updated");
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish or unpublish a therapist profile")
    public ApiResponse<TherapistResponseDTO> publish(
            @PathVariable UUID id, @RequestParam boolean published) {
        TherapistResponseDTO saved = therapistService.updatePublishStatus(id, published);
        String action = published ? "THERAPIST_PUBLISHED" : "THERAPIST_UNPUBLISHED";
        auditLogService.record(action, "THERAPIST", id.toString(), Map.of("published", published));
        return ApiResponse.ok(saved, published ? "Therapist published" : "Therapist unpublished");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a therapist profile")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        therapistService.delete(id);
        auditLogService.record("THERAPIST_DELETED", "THERAPIST", id.toString(), Map.of());
        return ApiResponse.ok(null, "Therapist deleted");
    }
}
