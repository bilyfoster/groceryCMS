package com.brochure.cms.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brochure.cms.dto.TherapistRequestDTO;
import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSelfServiceDTO;
import com.brochure.cms.dto.TherapistSummaryDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.models.Therapist;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.repositories.TherapistRepository;
import com.brochure.cms.shared.dto.PagedResponse;
import com.brochure.cms.shared.exception.AccessDeniedException;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.domain.tenant.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TherapistServiceImplTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID THERAPIST_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID FOCUS_ID = UUID.randomUUID();

    @Mock
    private TherapistRepository therapistRepository;

    @Mock
    private TaxonomyTermRepository taxonomyTermRepository;

    private TherapistServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TherapistServiceImpl(therapistRepository, taxonomyTermRepository);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private TherapistRequestDTO adminRequest() {
        return TherapistRequestDTO.builder()
                .userId(USER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .credentials("LPC")
                .slug("jane-doe")
                .bio("Bio")
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .published(true)
                .sortOrder(1)
                .termIds(Set.of(FOCUS_ID))
                .build();
    }

    private TaxonomyTerm focusTerm() {
        return TaxonomyTerm.builder()
                .tenantId(TENANT_ID)
                .type(TaxonomyType.FOCUS_AREA)
                .label("Anxiety")
                .slug("anxiety")
                .sortOrder(0)
                .active(true)
                .build();
    }

    private Therapist existingTherapist() {
        return Therapist.builder()
                .tenantId(TENANT_ID)
                .userId(USER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .slug("jane-doe")
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .published(true)
                .sortOrder(1)
                .terms(Set.of())
                .build();
    }

    @Test
    void create_When_SlugIsUniqueAndTermsValid_Expect_TherapistSavedWithTenant() {
        TherapistRequestDTO request = adminRequest();
        TaxonomyTerm term = focusTerm();
        term.setId(FOCUS_ID);

        when(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(request.getSlug(), TENANT_ID))
                .thenReturn(false);
        when(taxonomyTermRepository.findByIdAndTenantIdAndDeletedAtIsNull(FOCUS_ID, TENANT_ID))
                .thenReturn(Optional.of(term));
        when(therapistRepository.save(any(Therapist.class))).thenAnswer(inv -> inv.getArgument(0));

        TherapistResponseDTO result = service.create(request);

        ArgumentCaptor<Therapist> captor = ArgumentCaptor.forClass(Therapist.class);
        verify(therapistRepository).save(captor.capture());
        Therapist saved = captor.getValue();
        assertEquals(TENANT_ID, saved.getTenantId());
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("jane-doe", saved.getSlug());
        assertEquals(1, saved.getTerms().size());
        assertEquals("Jane", result.getFirstName());
        assertEquals(1, result.getFocusAreas().size());
    }

    @Test
    void create_When_SlugAlreadyExists_Expect_ValidationExceptionAndNoSave() {
        TherapistRequestDTO request = adminRequest();
        when(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(request.getSlug(), TENANT_ID))
                .thenReturn(true);

        assertThrows(ValidationException.class, () -> service.create(request));
        verify(therapistRepository, never()).save(any());
    }

    @Test
    void create_When_TermMissing_Expect_ValidationException() {
        TherapistRequestDTO request = adminRequest();

        when(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(request.getSlug(), TENANT_ID))
                .thenReturn(false);
        when(taxonomyTermRepository.findByIdAndTenantIdAndDeletedAtIsNull(FOCUS_ID, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> service.create(request));
        verify(therapistRepository, never()).save(any());
    }

    @Test
    void update_When_SlugChangedToExisting_Expect_ValidationException() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        TherapistRequestDTO request = adminRequest();
        request.setSlug("taken-slug");

        when(therapistRepository.findByIdAndTenantIdFetchTerms(THERAPIST_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));
        when(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(
                request.getSlug(), TENANT_ID, THERAPIST_ID))
                .thenReturn(true);

        assertThrows(ValidationException.class, () -> service.update(THERAPIST_ID, request));
    }

    @Test
    void update_When_TermMissing_Expect_ValidationException() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        TherapistRequestDTO request = adminRequest();

        when(therapistRepository.findByIdAndTenantIdFetchTerms(THERAPIST_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));
        when(taxonomyTermRepository.findByIdAndTenantIdAndDeletedAtIsNull(FOCUS_ID, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> service.update(THERAPIST_ID, request));
    }

    @Test
    void delete_When_TherapistExists_Expect_SoftDeleted() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        when(therapistRepository.findByIdAndTenantIdFetchTerms(THERAPIST_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));
        when(therapistRepository.save(any(Therapist.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(THERAPIST_ID);

        assertTrue(therapist.isDeleted(), "therapist should be soft-deleted");
        verify(therapistRepository).save(therapist);
    }

    @Test
    void updatePublishStatus_When_TherapistExists_Expect_PublishedFlagToggled() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        therapist.setPublished(false);
        when(therapistRepository.findByIdAndTenantIdFetchTerms(THERAPIST_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));
        when(therapistRepository.save(any(Therapist.class))).thenAnswer(inv -> inv.getArgument(0));

        TherapistResponseDTO result = service.updatePublishStatus(THERAPIST_ID, true);

        assertTrue(result.isPublished());
    }

    @Test
    void findPublishedDirectory_When_TherapistsExist_Expect_PagedSummariesInOrder() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        Page<UUID> idPage = new PageImpl<>(List.of(THERAPIST_ID), PageRequest.of(0, 20), 1);

        when(therapistRepository.findPublishedDirectoryIds(
                TENANT_ID, null, TaxonomyType.FOCUS_AREA, null, TaxonomyType.MODALITY,
                null, TaxonomyType.DEMOGRAPHIC, null, null, null, idPage.getPageable()))
                .thenReturn(idPage);
        when(therapistRepository.findAllByIdsFetchTerms(TENANT_ID, List.of(THERAPIST_ID)))
                .thenReturn(List.of(therapist));

        PagedResponse<TherapistSummaryDTO> result = service.findPublishedDirectory(
                null, null, null, null, null, null, idPage.getPageable());

        assertEquals(1, result.totalElements());
        assertEquals(1, result.items().size());
        assertEquals("jane-doe", result.items().get(0).getSlug());
    }

    @Test
    void findPublishedBySlug_When_NotFound_Expect_ResourceNotFoundException() {
        when(therapistRepository.findPublishedBySlugAndTenantIdFetchTerms("missing", TENANT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findPublishedBySlug("missing"));
    }

    @Test
    void getOwnProfile_When_UserNotLinked_Expect_AccessDeniedException() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        therapist.setUserId(null);
        when(therapistRepository.findByUserIdAndTenantIdFetchTerms(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));

        assertThrows(AccessDeniedException.class, () -> service.getOwnProfile(USER_ID));
    }

    @Test
    void updateOwnProfile_When_Owned_Expect_ProfileUpdatedWithoutChangingPublishedOrSortOrder() {
        Therapist therapist = existingTherapist();
        therapist.setId(THERAPIST_ID);
        therapist.setUserId(USER_ID);
        therapist.setPublished(true);
        therapist.setSortOrder(5);

        TherapistSelfServiceDTO request = TherapistSelfServiceDTO.builder()
                .firstName("Janet")
                .lastName("Doe")
                .slug("janet-doe")
                .serviceDelivery(ServiceDelivery.VIRTUAL)
                .availabilityStatus(AvailabilityStatus.LIMITED)
                .termIds(Set.of())
                .build();

        when(therapistRepository.findByUserIdAndTenantIdFetchTerms(USER_ID, TENANT_ID))
                .thenReturn(Optional.of(therapist));
        when(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(
                request.getSlug(), TENANT_ID, THERAPIST_ID))
                .thenReturn(false);
        when(therapistRepository.save(any(Therapist.class))).thenAnswer(inv -> inv.getArgument(0));

        TherapistResponseDTO result = service.updateOwnProfile(USER_ID, request);

        assertEquals("Janet", result.getFirstName());
        assertEquals(ServiceDelivery.VIRTUAL, result.getServiceDelivery());
        assertTrue(result.isPublished(), "published should remain unchanged");
        assertEquals(5, result.getSortOrder(), "sortOrder should remain unchanged");
    }
}
