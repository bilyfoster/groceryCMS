package com.brochure.cms.dto;

import com.brochure.cms.enums.ServiceDelivery;
import jakarta.validation.constraints.Email;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public intake questionnaire payload used by the matching engine.
 *
 * <p>Captures client preferences only — no clinical or PHI data. All fields are
 * optional, but supplying at least one focus area is recommended for useful results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeRequestDTO {

    /** Focus-area taxonomy term ids selected by the client. */
    private Set<UUID> areasOfConcern;

    /** Modality taxonomy term ids the client prefers. */
    private Set<UUID> preferredModalities;

    /** Demographic taxonomy term id that best describes the client. */
    private UUID clientDemographic;

    /** Preferred session delivery method. */
    private ServiceDelivery preferredDelivery;

    /** Opt-in contact email; validated when supplied. */
    @Email(message = "contactEmail must be a valid email address")
    private String contactEmail;

    /** Optional free-text therapist gender preference (not persisted as clinical info). */
    private String therapistGenderPreference;

    /** Optional free-text therapist identity preference. */
    private String therapistIdentityPreference;
}
