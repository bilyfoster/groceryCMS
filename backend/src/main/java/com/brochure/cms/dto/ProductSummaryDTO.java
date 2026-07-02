package com.brochure.cms.dto;

import com.brochure.cms.enums.StockStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public directory card payload for a grocery product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {

    private UUID id;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private String unit;
    private String photoUrl;
    private StockStatus stockStatus;
    private List<TaxonomyTermResponseDTO> allergyTypes;
    private List<TaxonomyTermResponseDTO> dietTypes;
    private List<TaxonomyTermResponseDTO> categories;
    private int sortOrder;
}
