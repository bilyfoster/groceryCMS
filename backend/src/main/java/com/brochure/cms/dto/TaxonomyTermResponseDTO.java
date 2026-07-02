package com.brochure.cms.dto;

import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload representing a taxonomy term.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyTermResponseDTO {

    private UUID id;
    private TaxonomyType type;
    private String label;
    private String slug;
    private String description;
    private int sortOrder;
    private boolean active;

    /**
     * Maps a {@link TaxonomyTerm} entity to its response DTO.
     *
     * @param term the entity to map (must not be {@code null})
     * @return the populated response DTO
     */
    public static TaxonomyTermResponseDTO from(TaxonomyTerm term) {
        return TaxonomyTermResponseDTO.builder()
                .id(term.getId())
                .type(term.getType())
                .label(term.getLabel())
                .slug(term.getSlug())
                .description(term.getDescription())
                .sortOrder(term.getSortOrder())
                .active(term.isActive())
                .build();
    }
}
