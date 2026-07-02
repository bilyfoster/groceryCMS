package com.brochure.cms.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brochure.cms.dto.TaxonomyTermRequestDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.domain.tenant.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceImplTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private TaxonomyTermRepository repository;

    private TaxonomyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TaxonomyServiceImpl(repository);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private TaxonomyTermRequestDTO request() {
        return TaxonomyTermRequestDTO.builder()
                .type(TaxonomyType.FOCUS_AREA)
                .label("Anxiety")
                .slug("anxiety")
                .sortOrder(0)
                .build();
    }

    @Test
    void create_When_SlugIsUnique_Expect_TermSavedWithTenantAndActiveDefault() {
        when(repository.existsByTenantIdAndTypeAndSlugAndDeletedAtIsNull(
                        TENANT_ID, TaxonomyType.FOCUS_AREA, "anxiety"))
                .thenReturn(false);
        when(repository.save(any(TaxonomyTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxonomyTermResponseDTO result = service.create(request());

        ArgumentCaptor<TaxonomyTerm> captor = ArgumentCaptor.forClass(TaxonomyTerm.class);
        verify(repository).save(captor.capture());
        TaxonomyTerm saved = captor.getValue();
        assertEquals(TENANT_ID, saved.getTenantId());
        assertEquals("anxiety", saved.getSlug());
        assertTrue(saved.isActive(), "active should default to true when omitted");
        assertEquals("Anxiety", result.getLabel());
    }

    @Test
    void create_When_SlugAlreadyExists_Expect_ValidationExceptionAndNoSave() {
        when(repository.existsByTenantIdAndTypeAndSlugAndDeletedAtIsNull(
                        TENANT_ID, TaxonomyType.FOCUS_AREA, "anxiety"))
                .thenReturn(true);

        assertThrows(ValidationException.class, () -> service.create(request()));
        verify(repository, never()).save(any());
    }

    @Test
    void update_When_TermMissing_Expect_ResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndTenantIdAndDeletedAtIsNull(id, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request()));
    }

    @Test
    void delete_When_TermExists_Expect_SoftDeleted() {
        UUID id = UUID.randomUUID();
        TaxonomyTerm term = TaxonomyTerm.builder()
                .tenantId(TENANT_ID)
                .type(TaxonomyType.FOCUS_AREA)
                .label("Anxiety")
                .slug("anxiety")
                .active(true)
                .build();
        when(repository.findByIdAndTenantIdAndDeletedAtIsNull(id, TENANT_ID))
                .thenReturn(Optional.of(term));
        when(repository.save(any(TaxonomyTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(id);

        assertTrue(term.isDeleted(), "term should be soft-deleted");
        verify(repository).save(term);
    }

    @Test
    void listActive_When_TermsExist_Expect_MappedDtosInRepositoryOrder() {
        TaxonomyTerm term = TaxonomyTerm.builder()
                .tenantId(TENANT_ID)
                .type(TaxonomyType.MODALITY)
                .label("CBT")
                .slug("cbt")
                .active(true)
                .build();
        when(repository.findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(
                        TENANT_ID, TaxonomyType.MODALITY))
                .thenReturn(List.of(term));

        List<TaxonomyTermResponseDTO> result = service.listActive(TaxonomyType.MODALITY);

        assertEquals(1, result.size());
        assertEquals("CBT", result.get(0).getLabel());
    }
}
