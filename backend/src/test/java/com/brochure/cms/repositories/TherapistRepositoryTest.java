package com.brochure.cms.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.models.Therapist;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TherapistRepositoryTest {

    private static final UUID TENANT_A = UUID.randomUUID();
    private static final UUID TENANT_B = UUID.randomUUID();

    @Autowired
    private TherapistRepository therapistRepository;

    @Autowired
    private TaxonomyTermRepository taxonomyTermRepository;

    @Test
    void findPublishedBySlugAndTenantIdFetchTerms_When_PublishedAndTenantMatches_Expect_Returned() {
        Therapist therapist = saveTherapist(TENANT_A, "dr-smith", true, AvailabilityStatus.ACCEPTING);

        Optional<Therapist> result = therapistRepository.findPublishedBySlugAndTenantIdFetchTerms(
                "dr-smith", TENANT_A);

        assertTrue(result.isPresent());
        assertEquals(therapist.getId(), result.get().getId());
    }

    @Test
    void findPublishedBySlugAndTenantIdFetchTerms_When_DifferentTenant_Expect_Empty() {
        saveTherapist(TENANT_A, "dr-smith", true, AvailabilityStatus.ACCEPTING);

        Optional<Therapist> result = therapistRepository.findPublishedBySlugAndTenantIdFetchTerms(
                "dr-smith", TENANT_B);

        assertTrue(result.isEmpty());
    }

    @Test
    void findPublishedBySlugAndTenantIdFetchTerms_When_Unpublished_Expect_Empty() {
        saveTherapist(TENANT_A, "dr-smith", false, AvailabilityStatus.ACCEPTING);

        Optional<Therapist> result = therapistRepository.findPublishedBySlugAndTenantIdFetchTerms(
                "dr-smith", TENANT_A);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsBySlugAndTenantIdAndDeletedAtIsNull_When_SlugExists_Expect_True() {
        saveTherapist(TENANT_A, "dr-jones", true, AvailabilityStatus.ACCEPTING);

        assertTrue(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull("dr-jones", TENANT_A));
        assertFalse(therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull("dr-jones", TENANT_B));
    }

    @Test
    void findPublishedDirectoryIds_When_FilteredByTerm_Expect_OnlyMatchingTherapist() {
        TaxonomyTerm focusArea = saveTerm(TENANT_A, TaxonomyType.FOCUS_AREA, "Anxiety", "anxiety");
        Therapist matching = saveTherapistWithTerm(TENANT_A, "anxiety-match", focusArea);
        saveTherapist(TENANT_A, "no-match", true, AvailabilityStatus.ACCEPTING);

        Page<UUID> result = therapistRepository.findPublishedDirectoryIds(
                TENANT_A,
                focusArea.getId(), TaxonomyType.FOCUS_AREA,
                null, TaxonomyType.MODALITY,
                null, TaxonomyType.DEMOGRAPHIC,
                null, null, null,
                PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(matching.getId(), result.getContent().get(0));
    }

    @Test
    void findAllByIdsFetchTerms_When_TherapistHasTerms_Expect_TermsLoaded() {
        TaxonomyTerm term = saveTerm(TENANT_A, TaxonomyType.MODALITY, "CBT", "cbt");
        Therapist therapist = saveTherapistWithTerm(TENANT_A, "with-terms", term);

        List<Therapist> result = therapistRepository.findAllByIdsFetchTerms(TENANT_A, List.of(therapist.getId()));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTerms().size());
        assertEquals(term.getId(), result.get(0).getTerms().iterator().next().getId());
    }

    private TaxonomyTerm saveTerm(UUID tenantId, TaxonomyType type, String label, String slug) {
        TaxonomyTerm term = TaxonomyTerm.builder()
                .tenantId(tenantId)
                .type(type)
                .label(label)
                .slug(slug)
                .sortOrder(0)
                .active(true)
                .build();
        return taxonomyTermRepository.save(term);
    }

    private Therapist saveTherapist(UUID tenantId, String slug, boolean published, AvailabilityStatus availability) {
        Therapist therapist = Therapist.builder()
                .tenantId(tenantId)
                .firstName("Test")
                .lastName("Therapist")
                .slug(slug)
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(availability)
                .published(published)
                .sortOrder(0)
                .terms(Set.of())
                .build();
        return therapistRepository.save(therapist);
    }

    private Therapist saveTherapistWithTerm(UUID tenantId, String slug, TaxonomyTerm term) {
        Therapist therapist = Therapist.builder()
                .tenantId(tenantId)
                .firstName("Test")
                .lastName("Therapist")
                .slug(slug)
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .published(true)
                .sortOrder(0)
                .terms(Set.of(term))
                .build();
        return therapistRepository.save(therapist);
    }
}
