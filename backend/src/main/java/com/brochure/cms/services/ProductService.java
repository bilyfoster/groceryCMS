package com.brochure.cms.services;

import com.brochure.cms.dto.ProductRequestDTO;
import com.brochure.cms.dto.ProductResponseDTO;
import com.brochure.cms.dto.ProductSummaryDTO;
import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.shared.dto.PagedResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Business operations for grocery product catalog entries. All operations are
 * scoped to the current tenant.
 */
public interface ProductService {

    /**
     * Lists all non-deleted products for the current tenant (admin view).
     *
     * @return product summaries ordered by sort order then name
     */
    List<ProductResponseDTO> listAll();

    /**
     * Retrieves a product by id for the current tenant.
     *
     * @param id the product id
     * @return the full product
     */
    ProductResponseDTO getById(UUID id);

    /**
     * Creates a new product.
     *
     * @param request the product data
     * @return the created full product
     */
    ProductResponseDTO create(ProductRequestDTO request);

    /**
     * Updates an existing product.
     *
     * @param id      the product id
     * @param request the updated product data
     * @return the updated full product
     */
    ProductResponseDTO update(UUID id, ProductRequestDTO request);

    /**
     * Soft-deletes a product.
     *
     * @param id the product id
     */
    void delete(UUID id);

    /**
     * Sets the published flag for a product.
     *
     * @param id        the product id
     * @param published the desired published state
     * @return the updated full product
     */
    ProductResponseDTO updatePublishStatus(UUID id, boolean published);

    /**
     * Public directory query with optional filters and pagination.
     *
     * @param allergyTypeId allergy-free term id filter
     * @param dietTypeId    diet type term id filter
     * @param categoryId    product category term id filter
     * @param storeSection  store section filter
     * @param stockStatus   stock status filter
     * @param search        free-text search
     * @param pageable      pagination and sort
     * @return a page of product summaries
     */
    PagedResponse<ProductSummaryDTO> findPublishedDirectory(
            UUID allergyTypeId,
            UUID dietTypeId,
            UUID categoryId,
            StoreSection storeSection,
            StockStatus stockStatus,
            String search,
            Pageable pageable);

    /**
     * Retrieves a published product by slug.
     *
     * @param slug the product slug
     * @return the full product
     */
    ProductResponseDTO findPublishedBySlug(String slug);
}
