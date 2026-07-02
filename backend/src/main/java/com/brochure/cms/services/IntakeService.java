package com.brochure.cms.services;

import com.brochure.cms.dto.IntakeCriterion;
import com.brochure.cms.dto.IntakeQuestionnaireDTO;
import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.domain.tenant.TenantRepository;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configurable allergy-awareness intake questionnaire and product recommendation.
 */
@Service
@Transactional(readOnly = true)
public class IntakeService {

    private static final String SETTINGS_KEY = "intakeQuestionnaire";

    private static final Map<String, String> SYMPTOM_OPTIONS = Map.ofEntries(
            Map.entry("bloating", "Bloating or gas"),
            Map.entry("stomach-pain", "Stomach pain or cramps"),
            Map.entry("diarrhea", "Diarrhea"),
            Map.entry("skin-rash", "Skin rash or hives"),
            Map.entry("itching", "Itching or tingling in mouth"),
            Map.entry("headaches", "Headaches"),
            Map.entry("fatigue", "Fatigue after eating"),
            Map.entry("joint-pain", "Joint pain or swelling"));

    private final TenantRepository tenantRepository;
    private final TaxonomyTermRepository taxonomyTermRepository;
    private final MatchingService matchingService;
    private final ObjectMapper objectMapper;

    public IntakeService(TenantRepository tenantRepository,
                         TaxonomyTermRepository taxonomyTermRepository,
                         MatchingService matchingService,
                         ObjectMapper objectMapper) {
        this.tenantRepository = tenantRepository;
        this.taxonomyTermRepository = taxonomyTermRepository;
        this.matchingService = matchingService;
        this.objectMapper = objectMapper;
    }

    public IntakeQuestionnaireDTO.Questionnaire getQuestionnaire() {
        UUID tenantId = TenantIds.current();
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> t.getDeletedAt() == null && t.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Object configured = tenant.getSettings().get(SETTINGS_KEY);
        if (configured != null) {
            return objectMapper.convertValue(configured, IntakeQuestionnaireDTO.Questionnaire.class);
        }
        return defaultQuestionnaire(tenantId);
    }

    @Transactional
    public MatchResponseDTO match(IntakeQuestionnaireDTO.MatchRequest request) {
        IntakeQuestionnaireDTO.Questionnaire questionnaire = getQuestionnaire();
        IntakeRequestDTO intake = translateAnswers(request.answers(), questionnaire);
        return matchingService.findMatches(intake);
    }

    private IntakeRequestDTO translateAnswers(Map<String, List<String>> answers,
                                               IntakeQuestionnaireDTO.Questionnaire questionnaire) {
        Set<UUID> avoidedAllergyIds = new HashSet<>();
        Set<UUID> dietIds = new HashSet<>();
        Set<UUID> categoryIds = new HashSet<>();
        Set<String> symptoms = new HashSet<>();

        Map<String, IntakeQuestionnaireDTO.Question> questionById = questionnaire.questions().stream()
                .collect(Collectors.toMap(IntakeQuestionnaireDTO.Question::id, q -> q));

        for (Map.Entry<String, List<String>> entry : nullToEmpty(answers).entrySet()) {
            IntakeQuestionnaireDTO.Question question = questionById.get(entry.getKey());
            if (question == null) {
                continue;
            }
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            try {
                switch (question.criterion()) {
                    case ALLERGY_TYPE -> avoidedAllergyIds.addAll(parseUuids(values));
                    case DIET_TYPE -> dietIds.addAll(parseUuids(values));
                    case PRODUCT_CATEGORY -> categoryIds.addAll(parseUuids(values));
                    case SYMPTOM -> symptoms.addAll(values);
                    case STORE_SECTION -> {
                        // Store section is not used for product matching; ignored here.
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed answers.
            }
        }

        return IntakeRequestDTO.builder()
                .avoidedAllergies(avoidedAllergyIds.isEmpty() ? null : avoidedAllergyIds)
                .preferredDiets(dietIds.isEmpty() ? null : dietIds)
                .preferredCategories(categoryIds.isEmpty() ? null : categoryIds)
                .symptoms(symptoms.isEmpty() ? null : symptoms)
                .build();
    }

    private IntakeQuestionnaireDTO.Questionnaire defaultQuestionnaire(UUID tenantId) {
        List<TaxonomyTerm> allergies = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.ALLERGY_TYPE);
        List<TaxonomyTerm> diets = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.DIET_TYPE);
        List<TaxonomyTerm> categories = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.PRODUCT_CATEGORY);

        List<IntakeQuestionnaireDTO.Question> questions = new ArrayList<>();

        if (!allergies.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "avoided_allergies",
                    "Which allergens do you need to avoid?",
                    "multi",
                    IntakeCriterion.ALLERGY_TYPE,
                    false,
                    allergies.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }

        questions.add(new IntakeQuestionnaireDTO.Question(
                "symptoms",
                "Have you noticed any of these symptoms after eating?",
                "multi",
                IntakeCriterion.SYMPTOM,
                false,
                SYMPTOM_OPTIONS.entrySet().stream()
                        .map(e -> new IntakeQuestionnaireDTO.Option(e.getKey(), e.getValue()))
                        .toList()));

        if (!diets.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "preferred_diets",
                    "Do you follow any of these diets?",
                    "multi",
                    IntakeCriterion.DIET_TYPE,
                    false,
                    diets.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }

        if (!categories.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "preferred_categories",
                    "What kinds of items are you looking for?",
                    "multi",
                    IntakeCriterion.PRODUCT_CATEGORY,
                    false,
                    categories.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }

        return new IntakeQuestionnaireDTO.Questionnaire(
                "Find safe, gluten-free & allergy-friendly foods",
                "Tell us what you avoid and how you feel after eating. We’ll suggest products that fit your needs and highlight symptoms worth discussing with a healthcare provider.",
                questions);
    }

    private List<TaxonomyTerm> activeTerms(UUID tenantId, com.brochure.cms.enums.TaxonomyType type) {
        return taxonomyTermRepository.findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(
                tenantId, type);
    }

    private static Map<String, List<String>> nullToEmpty(Map<String, List<String>> map) {
        return map == null ? Map.of() : map;
    }

    private static List<UUID> parseUuids(List<String> values) {
        return values.stream()
                .map(IntakeService::parseUuid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
