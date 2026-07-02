package com.brochure.cms.models;

import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An admin-managed taxonomy term (e.g. a focus area, modality, or demographic)
 * that can be attached to therapists for directory filtering and matching.
 *
 * <p>Uses {@code @Getter}/{@code @Setter} rather than {@code @Data} because the
 * class extends {@link BaseEntity}; {@code @Data} would generate {@code equals}/
 * {@code hashCode} that conflict with the inherited identity (coding guidelines §9).
 */
@Entity
@Table(name = "taxonomy_terms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyTerm extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaxonomyType type;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;
}
