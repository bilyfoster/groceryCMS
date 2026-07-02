package com.brochure.cms.dto;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full therapist profile payload returned by detail and admin endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistResponseDTO {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String credentials;
    private String pronouns;
    private String photoUrl;
    private String slug;
    private String bio;
    private Integer yearsOfExperience;
    private String education;
    private String licensure;
    private ServiceDelivery serviceDelivery;
    private AvailabilityStatus availabilityStatus;
    private String schedulingUrl;
    private String bookingPlatformRef;
    private String metaTitle;
    private String metaDescription;
    private String ogImageUrl;
    private String canonicalUrl;
    private boolean published;
    private int sortOrder;
    private List<TaxonomyTermResponseDTO> focusAreas;
    private List<TaxonomyTermResponseDTO> modalities;
    private List<TaxonomyTermResponseDTO> demographics;
}
