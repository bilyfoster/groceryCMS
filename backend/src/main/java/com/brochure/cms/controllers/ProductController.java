package com.brochure.cms.controllers;

import com.brochure.cms.dto.ProductResponseDTO;
import com.brochure.cms.dto.ProductSummaryDTO;
import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.services.ProductService;
import com.brochure.cms.shared.dto.ApiResponse;
import com.brochure.cms.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public product directory and detail API.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products (public)")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 24;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List published products with optional filters")
    public ApiResponse<PagedResponse<ProductSummaryDTO>> directory(
            @RequestParam(required = false) UUID allergyType,
            @RequestParam(required = false) UUID dietType,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) StoreSection storeSection,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(required = false) String q,
            @Parameter(hidden = true) @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return ApiResponse.ok(productService.findPublishedDirectory(
                allergyType, dietType, category, storeSection, stockStatus, q, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a published product by slug")
    public ApiResponse<ProductResponseDTO> detail(@PathVariable String slug) {
        return ApiResponse.ok(productService.findPublishedBySlug(slug));
    }
}
