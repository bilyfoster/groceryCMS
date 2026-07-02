package com.brochure.cms.dto;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload a therapist may use to edit their own profile.
 *
 * <p>Excludes fields that are managed by administrators: {@code userId},
 * {@code published}, and {@code sortOrder}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistSelfServiceDTO {

    public static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String SLUG_MESSAGE = "slug must be lowercase alphanumeric words separated by hyphens";

    @NotBlank
    @Size(max = 255)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    private String lastName;

    @Size(max = 500)
    private String credentials;

    @Size(max = 100)
    private String pronouns;

    @Size(max = 2000)
    private String photoUrl;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = SLUG_PATTERN, message = SLUG_MESSAGE)
    private String slug;

    @Size(max = 10000)
    private String bio;

    @Min(0)
    @Max(100)
    private Integer yearsOfExperience;

    @Size(max = 2000)
    private String education;

    @Size(max = 500)
    private String licensure;

    @NotNull
    private ServiceDelivery serviceDelivery;

    @NotNull
    private AvailabilityStatus availabilityStatus;

    @Size(max = 2000)
    private String schedulingUrl;

    @Size(max = 100)
    private String bookingPlatformRef;

    @Size(max = 500)
    private String metaTitle;

    @Size(max = 1000)
    private String metaDescription;

    @Size(max = 2000)
    private String ogImageUrl;

    @Size(max = 2000)
    private String canonicalUrl;

    private Set<UUID> termIds;
}
