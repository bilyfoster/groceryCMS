package com.brochure.cms.models;

import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A grocery product catalog entry (food item) stored per tenant.
 *
 * <p>Products are classified by admin-managed {@link TaxonomyTerm} rows for
 * allergens they are free of, diet types, and product categories. Uses
 * {@code @Getter}/{@code @Setter} rather than {@code @Data} because the class
 * extends {@link BaseEntity}; {@code @Data} would generate {@code equals}/
 * {@code hashCode} that conflict with the inherited identity (coding guidelines §9).
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(length = 255)
    private String brand;

    @Column(columnDefinition = "text")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String unit;

    @Column(name = "photo_url", length = 2000)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 32)
    private StockStatus stockStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_section", nullable = false, length = 32)
    private StoreSection storeSection;

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    @Column(name = "og_image_url", length = 2000)
    private String ogImageUrl;

    @Column(name = "canonical_url", length = 2000)
    private String canonicalUrl;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToMany
    @JoinTable(
            name = "product_terms",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "term_id"))
    private Set<TaxonomyTerm> terms;
}
