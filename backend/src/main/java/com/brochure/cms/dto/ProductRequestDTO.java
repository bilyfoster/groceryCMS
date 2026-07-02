package com.brochure.cms.dto;

import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin create/update payload for a grocery product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    public static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String SLUG_MESSAGE = "slug must be lowercase alphanumeric words separated by hyphens";

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = SLUG_PATTERN, message = SLUG_MESSAGE)
    private String slug;

    @Size(max = 255)
    private String brand;

    @Size(max = 10000)
    private String description;

    @DecimalMin(value = "0.00", inclusive = true)
    @DecimalMax(value = "999999.99", inclusive = true)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @Size(max = 50)
    private String unit;

    @Size(max = 2000)
    private String photoUrl;

    @NotNull
    private StockStatus stockStatus;

    @NotNull
    private StoreSection storeSection;

    @Size(max = 500)
    private String metaTitle;

    @Size(max = 1000)
    private String metaDescription;

    @Size(max = 2000)
    private String ogImageUrl;

    @Size(max = 2000)
    private String canonicalUrl;

    @NotNull
    private Boolean published;

    @NotNull
    @PositiveOrZero
    private Integer sortOrder;

    private Set<UUID> termIds;
}
