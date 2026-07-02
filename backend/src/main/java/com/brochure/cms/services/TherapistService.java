package com.brochure.cms.services;

import com.brochure.cms.dto.TherapistRequestDTO;
import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSelfServiceDTO;
import com.brochure.cms.dto.TherapistSummaryDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

import com.brochure.cms.shared.dto.PagedResponse;

/**
 * Business operations for therapist profiles. All operations are scoped to the
 * current tenant, and therapist self-service is ownership-restricted.
 */
public interface TherapistService {

    /**
     * Lists all non-deleted therapist profiles for the current tenant (admin view).
     *
     * @return therapist summaries ordered by sort order then name
     */
    List<TherapistResponseDTO> listAll();

    /**
     * Retrieves a therapist by id for the current tenant.
     *
     * @param id the therapist id
     * @return the full therapist profile
     */
    TherapistResponseDTO getById(UUID id);

    /**
     * Creates a new therapist profile.
     *
     * @param request the therapist data
     * @return the created full profile
     */
    TherapistResponseDTO create(TherapistRequestDTO request);

    /**
     * Updates an existing therapist profile.
     *
     * @param id      the therapist id
     * @param request the updated therapist data
     * @return the updated full profile
     */
    TherapistResponseDTO update(UUID id, TherapistRequestDTO request);

    /**
     * Soft-deletes a therapist profile.
     *
     * @param id the therapist id
     */
    void delete(UUID id);

    /**
     * Sets the published flag for a therapist profile.
     *
     * @param id        the therapist id
     * @param published the desired published state
     * @return the updated full profile
     */
    TherapistResponseDTO updatePublishStatus(UUID id, boolean published);

    /**
     * Public directory query with optional filters and pagination.
     *
     * @param focusAreaId  focus area term id filter
     * @param modalityId   modality term id filter
     * @param demographicId demographic term id filter
     * @param delivery     service delivery filter
     * @param availability availability status filter
     * @param search       free-text search
     * @param pageable     pagination and sort
     * @return a page of therapist summaries
     */
    PagedResponse<TherapistSummaryDTO> findPublishedDirectory(
            UUID focusAreaId,
            UUID modalityId,
            UUID demographicId,
            ServiceDelivery delivery,
            AvailabilityStatus availability,
            String search,
            Pageable pageable);

    /**
     * Retrieves a published therapist profile by slug.
     *
     * @param slug the therapist slug
     * @return the full therapist profile
     */
    TherapistResponseDTO findPublishedBySlug(String slug);

    /**
     * Retrieves the therapist profile linked to the given user id.
     *
     * @param userId the authenticated user id
     * @return the full therapist profile
     */
    TherapistResponseDTO getOwnProfile(UUID userId);

    /**
     * Updates the therapist profile linked to the given user id.
     *
     * @param userId  the authenticated user id
     * @param request the self-service update payload
     * @return the updated full profile
     */
    TherapistResponseDTO updateOwnProfile(UUID userId, TherapistSelfServiceDTO request);
}
