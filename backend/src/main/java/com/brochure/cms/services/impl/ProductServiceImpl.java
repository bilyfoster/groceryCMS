package com.brochure.cms.services.impl;

import com.brochure.cms.dto.ProductRequestDTO;
import com.brochure.cms.dto.ProductResponseDTO;
import com.brochure.cms.dto.ProductSummaryDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.Product;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.repositories.ProductRepository;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.services.ProductService;
import com.brochure.cms.shared.dto.PagedResponse;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link ProductService} implementation backed by JPA. Every query is
 * scoped to {@link TenantIds#current()}.
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final Set<TaxonomyType> ALLOWED_TERM_TYPES = EnumSet.of(
            TaxonomyType.ALLERGY_TYPE, TaxonomyType.DIET_TYPE, TaxonomyType.PRODUCT_CATEGORY);

    private static final String PRODUCT_NOT_FOUND = "Product not found: ";
    private static final String SLUG_ALREADY_EXISTS = "A product with slug '%s' already exists in this tenant";

    private final ProductRepository productRepository;
    private final TaxonomyTermRepository taxonomyTermRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              TaxonomyTermRepository taxonomyTermRepository) {
        this.productRepository = productRepository;
        this.taxonomyTermRepository = taxonomyTermRepository;
    }

    @Override
    public List<ProductResponseDTO> listAll() {
        UUID tenantId = TenantIds.current();
        return productRepository.findAllByTenantIdFetchTerms(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponseDTO getById(UUID id) {
        UUID tenantId = TenantIds.current();
        Product product = productRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        UUID tenantId = TenantIds.current();
        ensureSlugUnique(request.getSlug(), tenantId, null);
        Set<TaxonomyTerm> terms = resolveAndValidateTerms(request.getTermIds(), tenantId);

        Product product = Product.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .slug(request.getSlug())
                .brand(request.getBrand())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .photoUrl(request.getPhotoUrl())
                .stockStatus(request.getStockStatus())
                .storeSection(request.getStoreSection())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .ogImageUrl(request.getOgImageUrl())
                .canonicalUrl(request.getCanonicalUrl())
                .published(request.getPublished())
                .sortOrder(request.getSortOrder())
                .terms(terms)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product {} with slug '{}' for tenant {}", saved.getId(), saved.getSlug(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponseDTO update(UUID id, ProductRequestDTO request) {
        UUID tenantId = TenantIds.current();
        Product product = productRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));

        if (!product.getSlug().equals(request.getSlug())) {
            ensureSlugUnique(request.getSlug(), tenantId, id);
        }
        Set<TaxonomyTerm> terms = resolveAndValidateTerms(request.getTermIds(), tenantId);

        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setPhotoUrl(request.getPhotoUrl());
        product.setStockStatus(request.getStockStatus());
        product.setStoreSection(request.getStoreSection());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setOgImageUrl(request.getOgImageUrl());
        product.setCanonicalUrl(request.getCanonicalUrl());
        product.setPublished(request.getPublished());
        product.setSortOrder(request.getSortOrder());
        product.setTerms(terms);

        Product saved = productRepository.save(product);
        log.info("Updated product {} for tenant {}", id, tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantIds.current();
        Product product = productRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        product.softDelete();
        productRepository.save(product);
        log.info("Soft-deleted product {} for tenant {}", id, tenantId);
    }

    @Override
    @Transactional
    public ProductResponseDTO updatePublishStatus(UUID id, boolean published) {
        UUID tenantId = TenantIds.current();
        Product product = productRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        product.setPublished(published);
        Product saved = productRepository.save(product);
        log.info("Set published={} for product {} in tenant {}", published, id, tenantId);
        return toResponse(saved);
    }

    @Override
    public PagedResponse<ProductSummaryDTO> findPublishedDirectory(
            UUID allergyTypeId,
            UUID dietTypeId,
            UUID categoryId,
            StoreSection storeSection,
            StockStatus stockStatus,
            String search,
            Pageable pageable) {
        UUID tenantId = TenantIds.current();
        String searchPattern = (search == null || search.isBlank())
                ? null
                : "%" + search.trim().toLowerCase() + "%";
        Page<UUID> idPage = productRepository.findPublishedDirectoryIds(
                tenantId,
                allergyTypeId,
                TaxonomyType.ALLERGY_TYPE,
                dietTypeId,
                TaxonomyType.DIET_TYPE,
                categoryId,
                TaxonomyType.PRODUCT_CATEGORY,
                storeSection,
                stockStatus,
                searchPattern,
                pageable);

        if (idPage.isEmpty()) {
            return PagedResponse.of(List.of(), idPage.getNumber(), idPage.getSize(), idPage.getTotalElements());
        }

        List<Product> products = productRepository.findAllByIdsFetchTerms(tenantId, idPage.getContent());
        Map<UUID, Product> byId = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ProductSummaryDTO> content = idPage.getContent().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toSummary)
                .toList();

        return PagedResponse.of(content, idPage.getNumber(), idPage.getSize(), idPage.getTotalElements());
    }

    @Override
    public ProductResponseDTO findPublishedBySlug(String slug) {
        UUID tenantId = TenantIds.current();
        Product product = productRepository.findPublishedBySlugAndTenantIdFetchTerms(slug, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        return toResponse(product);
    }

    private void ensureSlugUnique(String slug, UUID tenantId, UUID excludeId) {
        boolean exists;
        if (excludeId == null) {
            exists = productRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(slug, tenantId);
        } else {
            exists = productRepository.existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(slug, tenantId, excludeId);
        }
        if (exists) {
            throw new ValidationException(SLUG_ALREADY_EXISTS.formatted(slug));
        }
    }

    private Set<TaxonomyTerm> resolveAndValidateTerms(Set<UUID> termIds, UUID tenantId) {
        if (termIds == null || termIds.isEmpty()) {
            return Set.of();
        }
        Set<TaxonomyTerm> resolved = new HashSet<>();
        for (UUID termId : termIds) {
            TaxonomyTerm term = taxonomyTermRepository.findByIdAndTenantIdAndDeletedAtIsNull(termId, tenantId)
                    .orElseThrow(() -> new ValidationException("Invalid taxonomy term id: " + termId));
            if (!ALLOWED_TERM_TYPES.contains(term.getType())) {
                throw new ValidationException("Term " + termId + " is not a valid product classification");
            }
            resolved.add(term);
        }
        return resolved;
    }

    private ProductResponseDTO toResponse(Product product) {
        Map<TaxonomyType, List<TaxonomyTermResponseDTO>> grouped = groupTerms(product.getTerms());
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .unit(product.getUnit())
                .photoUrl(product.getPhotoUrl())
                .stockStatus(product.getStockStatus())
                .storeSection(product.getStoreSection())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .ogImageUrl(product.getOgImageUrl())
                .canonicalUrl(product.getCanonicalUrl())
                .published(product.isPublished())
                .sortOrder(product.getSortOrder())
                .allergyTypes(grouped.getOrDefault(TaxonomyType.ALLERGY_TYPE, List.of()))
                .dietTypes(grouped.getOrDefault(TaxonomyType.DIET_TYPE, List.of()))
                .categories(grouped.getOrDefault(TaxonomyType.PRODUCT_CATEGORY, List.of()))
                .build();
    }

    private ProductSummaryDTO toSummary(Product product) {
        Map<TaxonomyType, List<TaxonomyTermResponseDTO>> grouped = groupTerms(product.getTerms());
        return ProductSummaryDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brand(product.getBrand())
                .price(product.getPrice())
                .unit(product.getUnit())
                .photoUrl(product.getPhotoUrl())
                .stockStatus(product.getStockStatus())
                .allergyTypes(grouped.getOrDefault(TaxonomyType.ALLERGY_TYPE, List.of()))
                .dietTypes(grouped.getOrDefault(TaxonomyType.DIET_TYPE, List.of()))
                .categories(grouped.getOrDefault(TaxonomyType.PRODUCT_CATEGORY, List.of()))
                .sortOrder(product.getSortOrder())
                .build();
    }

    private Map<TaxonomyType, List<TaxonomyTermResponseDTO>> groupTerms(Collection<TaxonomyTerm> terms) {
        if (terms == null || terms.isEmpty()) {
            return Map.of();
        }
        return terms.stream()
                .sorted(Comparator.comparingInt(TaxonomyTerm::getSortOrder).thenComparing(TaxonomyTerm::getLabel))
                .collect(Collectors.groupingBy(
                        TaxonomyTerm::getType,
                        Collectors.mapping(TaxonomyTermResponseDTO::from, Collectors.toList())));
    }
}
