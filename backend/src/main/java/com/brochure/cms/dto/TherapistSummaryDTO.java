package com.brochure.cms.dto;

import com.brochure.cms.enums.AvailabilityStatus;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public directory card payload for a therapist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistSummaryDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String credentials;
    private String pronouns;
    private String photoUrl;
    private String slug;
    private AvailabilityStatus availabilityStatus;
    private List<TaxonomyTermResponseDTO> focusAreas;
    private List<TaxonomyTermResponseDTO> modalities;
    private List<TaxonomyTermResponseDTO> demographics;
    private int sortOrder;
}
