package com.brochure.cms.controllers;

import com.brochure.cms.dto.ProductRequestDTO;
import com.brochure.cms.dto.ProductResponseDTO;
import com.brochure.cms.services.AuditLogService;
import com.brochure.cms.services.ProductService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API for managing the grocery product catalog. The {@code /api/admin/**} path
 * requires {@code EDITOR} or {@code ADMIN}; this controller reinforces that on
 * every endpoint.
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
@Tag(name = "Products (admin)")
public class AdminProductController {

    private final ProductService productService;
    private final AuditLogService auditLogService;

    public AdminProductController(ProductService productService, AuditLogService auditLogService) {
        this.productService = productService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List all products for the current tenant")
    public ApiResponse<List<ProductResponseDTO>> list() {
        return ApiResponse.ok(productService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ApiResponse<ProductResponseDTO> get(@PathVariable UUID id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a product")
    public ApiResponse<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO saved = productService.create(request);
        auditLogService.record("PRODUCT_CREATED", "PRODUCT", saved.getId().toString(),
                Map.of("name", saved.getName(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Product created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ApiResponse<ProductResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO saved = productService.update(id, request);
        auditLogService.record("PRODUCT_UPDATED", "PRODUCT", id.toString(),
                Map.of("name", saved.getName(), "slug", saved.getSlug()));
        return ApiResponse.ok(saved, "Product updated");
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish or unpublish a product")
    public ApiResponse<ProductResponseDTO> publish(
            @PathVariable UUID id, @RequestParam boolean published) {
        ProductResponseDTO saved = productService.updatePublishStatus(id, published);
        String action = published ? "PRODUCT_PUBLISHED" : "PRODUCT_UNPUBLISHED";
        auditLogService.record(action, "PRODUCT", id.toString(), Map.of("published", published));
        return ApiResponse.ok(saved, published ? "Product published" : "Product unpublished");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        auditLogService.record("PRODUCT_DELETED", "PRODUCT", id.toString(), Map.of());
        return ApiResponse.ok(null, "Product deleted");
    }
}
