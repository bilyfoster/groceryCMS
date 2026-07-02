package com.brochure.cms.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.dto.MatchResultDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.models.Therapist;
import com.brochure.cms.repositories.TherapistRepository;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.domain.tenant.Tenant;
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
class WeightedScoreMatchingServiceImplTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ANXIETY_ID = UUID.randomUUID();
    private static final UUID TRAUMA_ID = UUID.randomUUID();
    private static final UUID DEPRESSION_ID = UUID.randomUUID();
    private static final UUID CBT_ID = UUID.randomUUID();
    private static final UUID EMDR_ID = UUID.randomUUID();
    private static final UUID ADULTS_ID = UUID.randomUUID();

    @Mock
    private TherapistRepository therapistRepository;

    private WeightedScoreMatchingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WeightedScoreMatchingServiceImpl(therapistRepository);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void emptyIntake_When_PublishedTherapistsExist_Expect_AvailabilityBonusOnlyAndTieBreakOrdering() {
        Therapist second = therapist("Second", "Smith", AvailabilityStatus.ACCEPTING, 2, Set.of());
        Therapist first = therapist("First", "Adams", AvailabilityStatus.ACCEPTING, 1, Set.of());
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(second, first));

        MatchResponseDTO response = service.findMatches(IntakeRequestDTO.builder().build());

        List<MatchResultDTO> matches = response.getMatches();
        assertEquals(2, matches.size());
        assertEquals("Adams", matches.get(0).getLastName());
        assertEquals("Smith", matches.get(1).getLastName());
        assertEquals(1, matches.get(0).getRank());
        assertEquals(2, matches.get(1).getRank());
        assertEquals(0.10, matches.get(0).getScore(), 0.001);
        assertEquals(0.10, matches.get(1).getScore(), 0.001);
        assertTrue(matches.get(0).getExplanations().contains("Currently accepting new clients"));
    }

    @Test
    void availabilityGate_When_TherapistNotAccepting_Expect_Excluded() {
        Therapist accepting = therapist("Open", "Door", AvailabilityStatus.ACCEPTING, 1, Set.of());
        Therapist notAccepting = therapist("Closed", "Door", AvailabilityStatus.NOT_ACCEPTING, 0, Set.of());
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(accepting, notAccepting));

        MatchResponseDTO response = service.findMatches(emptyIntake());

        assertEquals(1, response.getMatches().size());
        assertEquals("Open", response.getMatches().get(0).getFirstName());
    }

    @Test
    void availabilityBonus_When_MultipleStatuses_Expect_DescendingOrder() {
        Therapist waitlist = therapist("Wait", "List", AvailabilityStatus.WAITLIST, 1, Set.of());
        Therapist limited = therapist("Limited", "Slot", AvailabilityStatus.LIMITED, 2, Set.of());
        Therapist accepting = therapist("Accepting", "All", AvailabilityStatus.ACCEPTING, 3, Set.of());
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID))
                .thenReturn(List.of(waitlist, limited, accepting));

        MatchResponseDTO response = service.findMatches(emptyIntake());

        List<MatchResultDTO> matches = response.getMatches();
        assertEquals(3, matches.size());
        assertEquals(AvailabilityStatus.ACCEPTING, matches.get(0).getAvailabilityStatus());
        assertEquals(AvailabilityStatus.LIMITED, matches.get(1).getAvailabilityStatus());
        assertEquals(AvailabilityStatus.WAITLIST, matches.get(2).getAvailabilityStatus());
        assertTrue(matches.get(0).getScore() > matches.get(1).getScore());
        assertTrue(matches.get(1).getScore() > matches.get(2).getScore());
    }

    @Test
    void allMatches_When_PerfectOverlap_Expect_ScoreOfOneAndFirstRank() {
        Therapist perfect = therapist("Perfect", "Match", AvailabilityStatus.ACCEPTING, 0,
                Set.of(
                        term(ANXIETY_ID, TaxonomyType.FOCUS_AREA, "Anxiety", 0),
                        term(TRAUMA_ID, TaxonomyType.FOCUS_AREA, "Trauma", 1),
                        term(CBT_ID, TaxonomyType.MODALITY, "CBT", 0),
                        term(ADULTS_ID, TaxonomyType.DEMOGRAPHIC, "Adults", 0)));
        perfect.setServiceDelivery(ServiceDelivery.VIRTUAL);
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(perfect));

        IntakeRequestDTO request = IntakeRequestDTO.builder()
                .areasOfConcern(Set.of(ANXIETY_ID, TRAUMA_ID))
                .preferredModalities(Set.of(CBT_ID))
                .clientDemographic(ADULTS_ID)
                .preferredDelivery(ServiceDelivery.VIRTUAL)
                .build();

        MatchResponseDTO response = service.findMatches(request);

        assertEquals(1, response.getMatches().size());
        MatchResultDTO result = response.getMatches().get(0);
        assertEquals(1.0, result.getScore(), 0.001);
        assertEquals(1, result.getRank());
    }

    @Test
    void noFocusOverlap_When_TherapistHasDifferentFocusAreas_Expect_LowerScoreAndNoOverlapExplanation() {
        Therapist therapist = therapist("Different", "Focus", AvailabilityStatus.ACCEPTING, 0,
                Set.of(term(DEPRESSION_ID, TaxonomyType.FOCUS_AREA, "Depression", 0)));
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(therapist));

        IntakeRequestDTO request = IntakeRequestDTO.builder()
                .areasOfConcern(Set.of(ANXIETY_ID))
                .build();

        MatchResponseDTO response = service.findMatches(request);

        assertEquals(1, response.getMatches().size());
        MatchResultDTO result = response.getMatches().get(0);
        assertEquals(0.10, result.getScore(), 0.001);
        assertTrue(result.getExplanations().contains("No focus area overlap"));
    }

    @Test
    void ties_When_SameScore_Expect_SortOrderThenLastNameBreak() {
        Therapist zSort = therapist("Zoe", "Alpha", AvailabilityStatus.ACCEPTING, 2, Set.of());
        Therapist aSort = therapist("Amy", "Beta", AvailabilityStatus.ACCEPTING, 1, Set.of());
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(zSort, aSort));

        MatchResponseDTO response = service.findMatches(emptyIntake());

        List<MatchResultDTO> matches = response.getMatches();
        assertEquals("Beta", matches.get(0).getLastName());
        assertEquals("Alpha", matches.get(1).getLastName());
    }

    @Test
    void weightBoundaries_When_RangeOfMatches_Expect_ScoresBetweenZeroAndOne() {
        Therapist partial = therapist("Partial", "Fit", AvailabilityStatus.LIMITED, 0,
                Set.of(
                        term(ANXIETY_ID, TaxonomyType.FOCUS_AREA, "Anxiety", 0),
                        term(ADULTS_ID, TaxonomyType.DEMOGRAPHIC, "Adults", 0)));
        partial.setServiceDelivery(ServiceDelivery.IN_PERSON);
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(partial));

        IntakeRequestDTO request = IntakeRequestDTO.builder()
                .areasOfConcern(Set.of(ANXIETY_ID, TRAUMA_ID))
                .clientDemographic(ADULTS_ID)
                .preferredDelivery(ServiceDelivery.VIRTUAL)
                .build();

        MatchResponseDTO response = service.findMatches(request);

        MatchResultDTO result = response.getMatches().get(0);
        assertTrue(result.getScore() >= 0.0 && result.getScore() <= 1.0,
                "Score should be within [0,1] but was " + result.getScore());
    }

    @Test
    void explanations_When_MultipleDimensionsMatch_Expect_HumanReadableMessages() {
        Therapist therapist = therapist("Helpful", "Therapist", AvailabilityStatus.ACCEPTING, 0,
                Set.of(
                        term(ANXIETY_ID, TaxonomyType.FOCUS_AREA, "Anxiety", 0),
                        term(TRAUMA_ID, TaxonomyType.FOCUS_AREA, "Trauma", 1),
                        term(CBT_ID, TaxonomyType.MODALITY, "CBT", 0),
                        term(EMDR_ID, TaxonomyType.MODALITY, "EMDR", 1),
                        term(ADULTS_ID, TaxonomyType.DEMOGRAPHIC, "Adults", 0)));
        therapist.setServiceDelivery(ServiceDelivery.HYBRID);
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(therapist));

        IntakeRequestDTO request = IntakeRequestDTO.builder()
                .areasOfConcern(Set.of(ANXIETY_ID, TRAUMA_ID, DEPRESSION_ID))
                .preferredModalities(Set.of(CBT_ID, EMDR_ID))
                .clientDemographic(ADULTS_ID)
                .preferredDelivery(ServiceDelivery.VIRTUAL)
                .build();

        MatchResponseDTO response = service.findMatches(request);

        List<String> explanations = response.getMatches().get(0).getExplanations();
        assertTrue(explanations.contains("Specializes in Anxiety, Trauma — 2 of your 3 focus areas"));
        assertTrue(explanations.contains("Offers CBT, EMDR — 2 of your 2 preferred modalities"));
        assertTrue(explanations.contains("Serves your demographic"));
        assertTrue(explanations.contains("Offers hybrid session delivery"));
        assertTrue(explanations.contains("Currently accepting new clients"));
    }

    @Test
    void nullRequest_Expect_TreatedAsEmptyIntake() {
        Therapist therapist = therapist("Solo", "Practitioner", AvailabilityStatus.ACCEPTING, 0, Set.of());
        when(therapistRepository.findAllByTenantIdFetchTerms(TENANT_ID)).thenReturn(List.of(therapist));

        MatchResponseDTO response = service.findMatches(null);

        assertEquals(1, response.getMatches().size());
    }

    private IntakeRequestDTO emptyIntake() {
        return IntakeRequestDTO.builder().build();
    }

    private Therapist therapist(String firstName, String lastName, AvailabilityStatus availability,
                                int sortOrder, Set<TaxonomyTerm> terms) {
        Therapist therapist = Therapist.builder()
                .tenantId(TENANT_ID)
                .firstName(firstName)
                .lastName(lastName)
                .slug(firstName.toLowerCase() + "-" + lastName.toLowerCase())
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(availability)
                .published(true)
                .sortOrder(sortOrder)
                .terms(terms)
                .build();
        therapist.setId(UUID.randomUUID());
        return therapist;
    }

    private TaxonomyTerm term(UUID id, TaxonomyType type, String label, int sortOrder) {
        TaxonomyTerm term = TaxonomyTerm.builder()
                .tenantId(TENANT_ID)
                .type(type)
                .label(label)
                .slug(label.toLowerCase())
                .sortOrder(sortOrder)
                .active(true)
                .build();
        term.setId(id);
        return term;
    }
}
