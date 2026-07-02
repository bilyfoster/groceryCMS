package com.brochure.cms.dto;

import com.brochure.cms.enums.TaxonomyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a taxonomy term.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyTermRequestDTO {

    @NotNull
    private TaxonomyType type;

    @NotBlank
    @Size(max = 255)
    private String label;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "slug must be lowercase alphanumeric words separated by hyphens")
    private String slug;

    @Size(max = 5000)
    private String description;

    @PositiveOrZero
    private int sortOrder;

    /** Defaults to {@code true} when omitted. */
    private Boolean active;
}
