package com.brochure.cms.services.impl;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.dto.MatchResultDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.models.Therapist;
import com.brochure.cms.repositories.TherapistRepository;
import com.brochure.cms.services.MatchingService;
import com.brochure.cms.shared.util.TenantIds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule-based weighted matching implementation.
 *
 * <p>Scores are deterministic and explainable. All weights and availability bonuses are
 * declared as named constants so the algorithm can be tuned without hunting for magic numbers.
 */
@Service
@Transactional(readOnly = true)
public class WeightedScoreMatchingServiceImpl implements MatchingService {

    private static final int DEFAULT_TOP_N = 5;

    private static final double WEIGHT_FOCUS = 0.45;
    private static final double WEIGHT_MODALITY = 0.20;
    private static final double WEIGHT_DEMOGRAPHIC = 0.15;
    private static final double WEIGHT_DELIVERY = 0.10;
    private static final double AVAILABILITY_BONUS_ACCEPTING = 0.10;
    private static final double AVAILABILITY_BONUS_LIMITED = 0.05;
    private static final double AVAILABILITY_BONUS_WAITLIST = 0.02;

    private static final Map<AvailabilityStatus, Double> AVAILABILITY_BONUS = new EnumMap<>(AvailabilityStatus.class);

    static {
        AVAILABILITY_BONUS.put(AvailabilityStatus.ACCEPTING, AVAILABILITY_BONUS_ACCEPTING);
        AVAILABILITY_BONUS.put(AvailabilityStatus.LIMITED, AVAILABILITY_BONUS_LIMITED);
        AVAILABILITY_BONUS.put(AvailabilityStatus.WAITLIST, AVAILABILITY_BONUS_WAITLIST);
    }

    private static final String FOCUS_MATCHED_TEMPLATE = "Specializes in %s — %d of your %d focus areas";
    private static final String FOCUS_NO_OVERLAP = "No focus area overlap";
    private static final String MODALITY_MATCHED_TEMPLATE = "Offers %s — %d of your %d preferred modalities";
    private static final String MODALITY_NO_OVERLAP = "No modality overlap";
    private static final String DEMOGRAPHIC_MATCHED = "Serves your demographic";
    private static final String DEMOGRAPHIC_NO_MATCH = "Does not serve your demographic";
    private static final String DELIVERY_MATCHED_TEMPLATE = "Offers %s session delivery";
    private static final String DELIVERY_NO_MATCH_TEMPLATE = "Does not offer %s session delivery";
    private static final String AVAILABILITY_ACCEPTING = "Currently accepting new clients";
    private static final String AVAILABILITY_LIMITED = "Limited availability for new clients";
    private static final String AVAILABILITY_WAITLIST = "Available via waitlist";

    private static final String LABEL_DELIMITER = ", ";

    private final TherapistRepository therapistRepository;

    public WeightedScoreMatchingServiceImpl(TherapistRepository therapistRepository) {
        this.therapistRepository = therapistRepository;
    }

    @Override
    public MatchResponseDTO findMatches(IntakeRequestDTO request) {
        return findMatches(request, DEFAULT_TOP_N);
    }

    @Override
    public MatchResponseDTO findMatches(IntakeRequestDTO request, int topN) {
        IntakeRequestDTO intake = request != null ? request : IntakeRequestDTO.builder().build();
        UUID tenantId = TenantIds.current();

        List<Therapist> candidates = therapistRepository.findAllByTenantIdFetchTerms(tenantId).stream()
                .filter(Therapist::isPublished)
                .filter(therapist -> therapist.getAvailabilityStatus() != AvailabilityStatus.NOT_ACCEPTING)
                .toList();

        List<ScoredTherapist> scored = candidates.stream()
                .map(therapist -> score(intake, therapist))
                .sorted(rankComparator())
                .limit(Math.max(topN, 0))
                .toList();

        List<MatchResultDTO> matches = new ArrayList<>(scored.size());
        int rank = 1;
        for (ScoredTherapist scoredTherapist : scored) {
            Therapist therapist = scoredTherapist.therapist();
            matches.add(MatchResultDTO.builder()
                    .therapistId(therapist.getId())
                    .slug(therapist.getSlug())
                    .firstName(therapist.getFirstName())
                    .lastName(therapist.getLastName())
                    .credentials(therapist.getCredentials())
                    .photoUrl(therapist.getPhotoUrl())
                    .availabilityStatus(therapist.getAvailabilityStatus())
                    .score(roundScore(scoredTherapist.score()))
                    .rank(rank++)
                    .explanations(scoredTherapist.explanations())
                    .build());
        }

        return MatchResponseDTO.builder().matches(matches).build();
    }

    private ScoredTherapist score(IntakeRequestDTO intake, Therapist therapist) {
        Set<UUID> intakeFocus = nullToEmpty(intake.getAreasOfConcern());
        Set<UUID> intakeModalities = nullToEmpty(intake.getPreferredModalities());
        Set<TaxonomyTerm> focusTerms = termsByType(therapist.getTerms(), TaxonomyType.FOCUS_AREA);
        Set<TaxonomyTerm> modalityTerms = termsByType(therapist.getTerms(), TaxonomyType.MODALITY);
        Set<TaxonomyTerm> demographicTerms = termsByType(therapist.getTerms(), TaxonomyType.DEMOGRAPHIC);

        Set<UUID> therapistFocusIds = idsOf(focusTerms);
        Set<UUID> therapistModalityIds = idsOf(modalityTerms);

        double focusScore = jaccard(intakeFocus, therapistFocusIds);
        double modalityScore = jaccard(intakeModalities, therapistModalityIds);
        double demographicScore = demographicScore(intake.getClientDemographic(), demographicTerms);
        double deliveryScore = deliveryMatch(intake.getPreferredDelivery(), therapist.getServiceDelivery()) ? 1.0 : 0.0;
        double availabilityBonus = AVAILABILITY_BONUS.getOrDefault(therapist.getAvailabilityStatus(), 0.0);

        double totalScore = (WEIGHT_FOCUS * focusScore)
                + (WEIGHT_MODALITY * modalityScore)
                + (WEIGHT_DEMOGRAPHIC * demographicScore)
                + (WEIGHT_DELIVERY * deliveryScore)
                + availabilityBonus;

        List<String> explanations = buildExplanations(
                intakeFocus, focusTerms,
                intakeModalities, modalityTerms,
                intake.getClientDemographic(), demographicTerms,
                intake.getPreferredDelivery(), therapist.getServiceDelivery(),
                therapist.getAvailabilityStatus());

        return new ScoredTherapist(therapist, totalScore, explanations);
    }

    private List<String> buildExplanations(
            Set<UUID> intakeFocus, Set<TaxonomyTerm> focusTerms,
            Set<UUID> intakeModalities, Set<TaxonomyTerm> modalityTerms,
            UUID clientDemographic, Set<TaxonomyTerm> demographicTerms,
            ServiceDelivery preferredDelivery, ServiceDelivery therapistDelivery,
            AvailabilityStatus availabilityStatus) {

        List<String> explanations = new ArrayList<>();

        if (!intakeFocus.isEmpty()) {
            List<String> matchedLabels = labelsOf(intakeFocus, focusTerms);
            if (matchedLabels.isEmpty()) {
                explanations.add(FOCUS_NO_OVERLAP);
            } else {
                explanations.add(String.format(
                        FOCUS_MATCHED_TEMPLATE, String.join(LABEL_DELIMITER, matchedLabels),
                        matchedLabels.size(), intakeFocus.size()));
            }
        }

        if (!intakeModalities.isEmpty()) {
            List<String> matchedLabels = labelsOf(intakeModalities, modalityTerms);
            if (matchedLabels.isEmpty()) {
                explanations.add(MODALITY_NO_OVERLAP);
            } else {
                explanations.add(String.format(
                        MODALITY_MATCHED_TEMPLATE, String.join(LABEL_DELIMITER, matchedLabels),
                        matchedLabels.size(), intakeModalities.size()));
            }
        }

        if (clientDemographic != null) {
            boolean matched = demographicTerms.stream().anyMatch(term -> term.getId().equals(clientDemographic));
            explanations.add(matched ? DEMOGRAPHIC_MATCHED : DEMOGRAPHIC_NO_MATCH);
        }

        if (preferredDelivery != null) {
            boolean matched = deliveryMatch(preferredDelivery, therapistDelivery);
            if (matched) {
                explanations.add(String.format(DELIVERY_MATCHED_TEMPLATE, formatDelivery(therapistDelivery)));
            } else {
                explanations.add(String.format(DELIVERY_NO_MATCH_TEMPLATE, formatDelivery(preferredDelivery)));
            }
        }

        String availabilityExplanation = availabilityExplanation(availabilityStatus);
        if (availabilityExplanation != null) {
            explanations.add(availabilityExplanation);
        }

        return explanations;
    }

    private static double jaccard(Set<UUID> left, Set<UUID> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }
        Set<UUID> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        Set<UUID> union = new HashSet<>(left);
        union.addAll(right);
        return (double) intersection.size() / union.size();
    }

    private static double demographicScore(UUID clientDemographic, Set<TaxonomyTerm> therapistDemographics) {
        if (clientDemographic == null || therapistDemographics.isEmpty()) {
            return 0.0;
        }
        return therapistDemographics.stream().anyMatch(term -> term.getId().equals(clientDemographic)) ? 1.0 : 0.0;
    }

    private static boolean deliveryMatch(ServiceDelivery preferred, ServiceDelivery offered) {
        if (preferred == null || offered == null) {
            return false;
        }
        if (preferred == offered) {
            return true;
        }
        return offered == ServiceDelivery.HYBRID;
    }

    private static String availabilityExplanation(AvailabilityStatus status) {
        return switch (status) {
            case ACCEPTING -> AVAILABILITY_ACCEPTING;
            case LIMITED -> AVAILABILITY_LIMITED;
            case WAITLIST -> AVAILABILITY_WAITLIST;
            case NOT_ACCEPTING -> null;
        };
    }

    private static List<String> labelsOf(Set<UUID> intakeIds, Set<TaxonomyTerm> therapistTerms) {
        return therapistTerms.stream()
                .filter(term -> intakeIds.contains(term.getId()))
                .sorted(Comparator.comparingInt(TaxonomyTerm::getSortOrder)
                        .thenComparing(TaxonomyTerm::getLabel, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(TaxonomyTerm::getLabel)
                .toList();
    }

    private static Set<TaxonomyTerm> termsByType(Collection<TaxonomyTerm> terms, TaxonomyType type) {
        if (terms == null || terms.isEmpty()) {
            return Set.of();
        }
        return terms.stream()
                .filter(term -> term.getType() == type)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<UUID> idsOf(Collection<TaxonomyTerm> terms) {
        if (terms == null || terms.isEmpty()) {
            return Set.of();
        }
        return terms.stream()
                .map(TaxonomyTerm::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<UUID> nullToEmpty(Set<UUID> set) {
        return set == null ? Set.of() : set;
    }

    private static String formatDelivery(ServiceDelivery delivery) {
        if (delivery == null) {
            return "";
        }
        return switch (delivery) {
            case VIRTUAL -> "virtual";
            case IN_PERSON -> "in-person";
            case HYBRID -> "hybrid";
        };
    }

    private static Comparator<ScoredTherapist> rankComparator() {
        return Comparator.comparingDouble(ScoredTherapist::score).reversed()
                .thenComparingInt(scored -> scored.therapist().getSortOrder())
                .thenComparing(scored -> scored.therapist().getLastName(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(scored -> scored.therapist().getFirstName(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(scored -> scored.therapist().getId());
    }

    private static double roundScore(double score) {
        return Math.round(score * 1000.0) / 1000.0;
    }

    private record ScoredTherapist(Therapist therapist, double score, List<String> explanations) {
    }
}
