package com.brochure.cms.controllers;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSelfServiceDTO;
import com.brochure.cms.services.TherapistService;
import com.brochure.cms.shared.dto.ApiResponse;
import com.brochure.cms.shared.exception.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Therapist self-service endpoints. A user with the {@code THERAPIST} role may
 * only read and update the therapist profile whose {@code userId} matches their
 * own user id.
 */
@RestController
@RequestMapping("/api/me/therapist")
@PreAuthorize("hasRole('THERAPIST')")
@Tag(name = "Therapist self-service")
public class MeTherapistController {

    private final TherapistService therapistService;

    public MeTherapistController(TherapistService therapistService) {
        this.therapistService = therapistService;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated therapist's own profile")
    public ApiResponse<TherapistResponseDTO> getProfile() {
        return ApiResponse.ok(therapistService.getOwnProfile(currentUserId()));
    }

    @PutMapping
    @Operation(summary = "Update the authenticated therapist's own profile")
    public ApiResponse<TherapistResponseDTO> updateProfile(
            @Valid @RequestBody TherapistSelfServiceDTO request) {
        return ApiResponse.ok(therapistService.updateOwnProfile(currentUserId(), request),
                "Profile updated");
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return user.getId();
    }
}
