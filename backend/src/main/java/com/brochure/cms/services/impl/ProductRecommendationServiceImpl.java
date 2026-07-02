package com.brochure.cms.services.impl;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.dto.MatchResultDTO;
import com.brochure.cms.enums.StockStatus;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.Product;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.repositories.ProductRepository;
import com.brochure.cms.services.MatchingService;
import com.brochure.cms.shared.util.TenantIds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
 * Rule-based product recommendation implementation for allergy-aware grocery shopping.
 *
 * <p>Scores are deterministic and explainable. Products containing an avoided allergen
 * are excluded entirely. Remaining candidates are scored by diet/category overlap and
 * stock availability. Symptom selections produce non-clinical awareness notes.
 */
@Service
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements MatchingService {

    private static final int DEFAULT_TOP_N = 8;

    private static final double WEIGHT_DIET = 0.40;
    private static final double WEIGHT_CATEGORY = 0.35;
    private static final double WEIGHT_STOCK = 0.25;

    private static final String DIET_MATCHED_TEMPLATE = "Matches %s diet — %d of your preferences";
    private static final String DIET_NO_OVERLAP = "No diet preference overlap";
    private static final String CATEGORY_MATCHED_TEMPLATE = "In categories you chose: %s — %d match";
    private static final String CATEGORY_NO_OVERLAP = "Not in your selected categories";
    private static final String STOCK_IN = "In stock";
    private static final String STOCK_LOW = "Running low — grab it soon";
    private static final String STOCK_OUT = "Currently out of stock";
    private static final String ALLERGEN_FREE_TEMPLATE = "Free of %s";
    private static final String LABEL_DELIMITER = ", ";

    private static final Map<String, String> SYMPTOM_AWARENESS = Map.ofEntries(
            Map.entry("bloating", "Bloating after meals can be a sign of several food intolerances. Consider tracking what you eat and discussing patterns with a healthcare provider."),
            Map.entry("stomach-pain", "Recurring stomach pain after eating is worth discussing with a clinician to rule out celiac disease, IBS, or other conditions."),
            Map.entry("diarrhea", "Frequent diarrhea after meals may indicate a food sensitivity or allergy — a healthcare provider can help you identify triggers safely."),
            Map.entry("skin-rash", "Skin rashes after eating can sometimes signal a food allergy. Seek medical advice, especially if breathing is affected."),
            Map.entry("itching", "Itching or tingling in the mouth after eating may be an oral allergy symptom — mention it to a healthcare provider."),
            Map.entry("headaches", "Headaches triggered by foods can have many causes; a food/symptom diary can help you and your provider spot patterns."),
            Map.entry("fatigue", "Fatigue after eating can be related to blood sugar or food sensitivities. A clinician can help you investigate."),
            Map.entry("joint-pain", "Joint pain linked to certain foods may be inflammatory; discuss with a healthcare provider before eliminating major food groups."));

    private final ProductRepository productRepository;

    public ProductRecommendationServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public MatchResponseDTO findMatches(IntakeRequestDTO request) {
        return findMatches(request, DEFAULT_TOP_N);
    }

    @Override
    public MatchResponseDTO findMatches(IntakeRequestDTO request, int topN) {
        IntakeRequestDTO intake = request != null ? request : IntakeRequestDTO.builder().build();
        UUID tenantId = TenantIds.current();

        Set<UUID> avoidedAllergies = nullToEmpty(intake.getAvoidedAllergies());
        Set<UUID> preferredDiets = nullToEmpty(intake.getPreferredDiets());
        Set<UUID> preferredCategories = nullToEmpty(intake.getPreferredCategories());
        Set<String> symptoms = nullToEmptyStrings(intake.getSymptoms());

        List<Product> candidates = productRepository.findAllByTenantIdFetchTerms(tenantId).stream()
                .filter(Product::isPublished)
                .filter(product -> claimsAvoidedAllergens(product, avoidedAllergies))
                .toList();

        List<ScoredProduct> scored = candidates.stream()
                .map(product -> score(product, preferredDiets, preferredCategories, avoidedAllergies))
                .sorted(rankComparator())
                .limit(Math.max(topN, 0))
                .toList();

        List<MatchResultDTO> matches = new ArrayList<>(scored.size());
        int rank = 1;
        for (ScoredProduct scoredProduct : scored) {
            Product product = scoredProduct.product();
            matches.add(MatchResultDTO.builder()
                    .productId(product.getId())
                    .slug(product.getSlug())
                    .name(product.getName())
                    .brand(product.getBrand())
                    .price(product.getPrice())
                    .unit(product.getUnit())
                    .photoUrl(product.getPhotoUrl())
                    .stockStatus(product.getStockStatus())
                    .score(roundScore(scoredProduct.score()))
                    .rank(rank++)
                    .explanations(scoredProduct.explanations())
                    .build());
        }

        return MatchResponseDTO.builder()
                .matches(matches)
                .awarenessNotes(buildAwarenessNotes(symptoms))
                .build();
    }

    private ScoredProduct score(Product product,
                                Set<UUID> preferredDiets,
                                Set<UUID> preferredCategories,
                                Set<UUID> avoidedAllergies) {
        Set<TaxonomyTerm> dietTerms = termsByType(product.getTerms(), TaxonomyType.DIET_TYPE);
        Set<TaxonomyTerm> categoryTerms = termsByType(product.getTerms(), TaxonomyType.PRODUCT_CATEGORY);
        Set<TaxonomyTerm> allergyTerms = termsByType(product.getTerms(), TaxonomyType.ALLERGY_TYPE);

        Set<UUID> dietIds = idsOf(dietTerms);
        Set<UUID> categoryIds = idsOf(categoryTerms);

        double dietScore = jaccard(preferredDiets, dietIds);
        double categoryScore = jaccard(preferredCategories, categoryIds);
        double stockScore = stockScore(product.getStockStatus());

        double totalScore = (WEIGHT_DIET * dietScore)
                + (WEIGHT_CATEGORY * categoryScore)
                + (WEIGHT_STOCK * stockScore);

        List<String> explanations = buildExplanations(
                preferredDiets, dietTerms,
                preferredCategories, categoryTerms,
                avoidedAllergies, allergyTerms,
                product.getStockStatus());

        return new ScoredProduct(product, totalScore, explanations);
    }

    private List<String> buildExplanations(
            Set<UUID> preferredDiets, Set<TaxonomyTerm> dietTerms,
            Set<UUID> preferredCategories, Set<TaxonomyTerm> categoryTerms,
            Set<UUID> avoidedAllergies, Set<TaxonomyTerm> allergyTerms,
            StockStatus stockStatus) {

        List<String> explanations = new ArrayList<>();

        if (!avoidedAllergies.isEmpty()) {
            List<String> matchedLabels = labelsOf(avoidedAllergies, allergyTerms);
            if (!matchedLabels.isEmpty()) {
                explanations.add(String.format(ALLERGEN_FREE_TEMPLATE, String.join(LABEL_DELIMITER, matchedLabels)));
            }
        }

        if (!preferredDiets.isEmpty()) {
            List<String> matchedLabels = labelsOf(preferredDiets, dietTerms);
            if (matchedLabels.isEmpty()) {
                explanations.add(DIET_NO_OVERLAP);
            } else {
                explanations.add(String.format(
                        DIET_MATCHED_TEMPLATE, String.join(LABEL_DELIMITER, matchedLabels),
                        matchedLabels.size()));
            }
        }

        if (!preferredCategories.isEmpty()) {
            List<String> matchedLabels = labelsOf(preferredCategories, categoryTerms);
            if (matchedLabels.isEmpty()) {
                explanations.add(CATEGORY_NO_OVERLAP);
            } else {
                explanations.add(String.format(
                        CATEGORY_MATCHED_TEMPLATE, String.join(LABEL_DELIMITER, matchedLabels),
                        matchedLabels.size()));
            }
        }

        String stockExplanation = stockExplanation(stockStatus);
        if (stockExplanation != null) {
            explanations.add(stockExplanation);
        }

        return explanations;
    }

    private static boolean claimsAvoidedAllergens(Product product, Set<UUID> avoidedAllergies) {
        if (avoidedAllergies.isEmpty()) {
            return true;
        }
        Set<UUID> productAllergyIds = idsOf(termsByType(product.getTerms(), TaxonomyType.ALLERGY_TYPE));
        for (UUID avoided : avoidedAllergies) {
            if (!productAllergyIds.contains(avoided)) {
                return false;
            }
        }
        return true;
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

    private static double stockScore(StockStatus status) {
        return switch (status) {
            case IN_STOCK -> 1.0;
            case LOW_STOCK -> 0.6;
            case OUT_OF_STOCK, DISCONTINUED -> 0.0;
        };
    }

    private static String stockExplanation(StockStatus status) {
        return switch (status) {
            case IN_STOCK -> STOCK_IN;
            case LOW_STOCK -> STOCK_LOW;
            case OUT_OF_STOCK -> STOCK_OUT;
            case DISCONTINUED -> null;
        };
    }

    private static List<String> labelsOf(Set<UUID> intakeIds, Set<TaxonomyTerm> productTerms) {
        return productTerms.stream()
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

    private static Set<String> nullToEmptyStrings(Set<String> set) {
        return set == null ? Set.of() : set;
    }

    private static List<String> buildAwarenessNotes(Set<String> symptoms) {
        Set<String> normalized = nullToEmptyStrings(symptoms).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<String> notes = new ArrayList<>();
        notes.add("This tool is not a medical diagnosis. Use these suggestions as a starting point for a conversation with a healthcare provider.");
        for (String symptom : normalized) {
            String note = SYMPTOM_AWARENESS.get(symptom);
            if (note != null) {
                notes.add(note);
            }
        }
        return notes;
    }

    private static Comparator<ScoredProduct> rankComparator() {
        return Comparator.comparingDouble(ScoredProduct::score).reversed()
                .thenComparingInt(scored -> scored.product().getSortOrder())
                .thenComparing(scored -> scored.product().getName(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(scored -> scored.product().getId());
    }

    private static double roundScore(double score) {
        return Math.round(score * 1000.0) / 1000.0;
    }

    private record ScoredProduct(Product product, double score, List<String> explanations) {
    }
}
