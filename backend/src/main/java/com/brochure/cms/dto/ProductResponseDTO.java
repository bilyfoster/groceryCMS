package com.brochure.cms.dto;

import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full product payload returned by detail and admin endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private UUID id;
    private String name;
    private String slug;
    private String brand;
    private String description;
    private BigDecimal price;
    private String unit;
    private String photoUrl;
    private StockStatus stockStatus;
    private StoreSection storeSection;
    private String metaTitle;
    private String metaDescription;
    private String ogImageUrl;
    private String canonicalUrl;
    private boolean published;
    private int sortOrder;
    private List<TaxonomyTermResponseDTO> allergyTypes;
    private List<TaxonomyTermResponseDTO> dietTypes;
    private List<TaxonomyTermResponseDTO> categories;
}
