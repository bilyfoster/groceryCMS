package com.brochure.cms.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.StoreSection;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.Product;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.repositories.ProductRepository;
import com.brochure.cms.shared.security.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductRecommendationServiceImplTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private ProductRepository productRepository;

    private ProductRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductRecommendationServiceImpl(productRepository);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setName("demo");
        tenant.setActive(true);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findMatches_When_AllergenAvoided_Expect_ProductExcluded() {
        UUID glutenId = UUID.randomUUID();
        TaxonomyTerm gluten = term(glutenId, TaxonomyType.ALLERGY_TYPE, "Gluten-Free");
        Product glutenFreeBread = product("gluten-free-bread", StockStatus.IN_STOCK, Set.of(gluten));
        Product regularBread = product("regular-bread", StockStatus.IN_STOCK, Set.of());

        when(productRepository.findAllByTenantIdFetchTerms(TENANT_ID))
                .thenReturn(List.of(glutenFreeBread, regularBread));

        IntakeRequestDTO intake = IntakeRequestDTO.builder()
                .avoidedAllergies(Set.of(glutenId))
                .build();

        MatchResponseDTO response = service.findMatches(intake);

        assertEquals(1, response.getMatches().size());
        assertEquals("gluten-free-bread", response.getMatches().get(0).getSlug());
        assertTrue(response.getAwarenessNotes().isEmpty());
    }

    @Test
    void findMatches_When_SymptomSelected_Expect_AwarenessNote() {
        Product bread = product("bread", StockStatus.IN_STOCK, Set.of());
        when(productRepository.findAllByTenantIdFetchTerms(TENANT_ID))
                .thenReturn(List.of(bread));

        IntakeRequestDTO intake = IntakeRequestDTO.builder()
                .symptoms(Set.of("bloating"))
                .build();

        MatchResponseDTO response = service.findMatches(intake);

        assertEquals(1, response.getMatches().size());
        assertTrue(response.getAwarenessNotes().stream().anyMatch(n -> n.contains("healthcare provider")));
    }

    private static TaxonomyTerm term(UUID id, TaxonomyType type, String label) {
        TaxonomyTerm term = new TaxonomyTerm();
        term.setId(id);
        term.setType(type);
        term.setLabel(label);
        term.setSlug(label.toLowerCase().replace(" ", "-"));
        term.setSortOrder(0);
        term.setActive(true);
        return term;
    }

    private static Product product(String slug, StockStatus stockStatus, Set<TaxonomyTerm> terms) {
        return Product.builder()
                .tenantId(TENANT_ID)
                .name(slug)
                .slug(slug)
                .brand("Test Brand")
                .price(BigDecimal.valueOf(4.99))
                .unit("12 oz")
                .stockStatus(stockStatus)
                .storeSection(StoreSection.PANTRY)
                .published(true)
                .sortOrder(0)
                .terms(terms)
                .build();
    }
}
