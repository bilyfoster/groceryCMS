package com.brochure.cms.services;

import com.brochure.cms.dto.IntakeCriterion;
import com.brochure.cms.dto.IntakeQuestionnaireDTO;
import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.enums.ServiceDelivery;
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

@Service
@Transactional(readOnly = true)
public class IntakeService {

    private static final String SETTINGS_KEY = "intakeQuestionnaire";

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
        Set<UUID> focusAreaIds = new HashSet<>();
        Set<UUID> modalityIds = new HashSet<>();
        UUID demographicId = null;
        ServiceDelivery delivery = null;

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
                    case FOCUS_AREA -> focusAreaIds.addAll(parseUuids(values));
                    case MODALITY -> modalityIds.addAll(parseUuids(values));
                    case DEMOGRAPHIC -> demographicId = parseUuid(values.get(0)).orElse(null);
                    case SERVICE_DELIVERY -> delivery = ServiceDelivery.valueOf(values.get(0));
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed answers.
            }
        }

        return IntakeRequestDTO.builder()
                .areasOfConcern(focusAreaIds.isEmpty() ? null : focusAreaIds)
                .preferredModalities(modalityIds.isEmpty() ? null : modalityIds)
                .clientDemographic(demographicId)
                .preferredDelivery(delivery)
                .build();
    }

    private IntakeQuestionnaireDTO.Questionnaire defaultQuestionnaire(UUID tenantId) {
        List<TaxonomyTerm> focusAreas = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.FOCUS_AREA);
        List<TaxonomyTerm> modalities = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.MODALITY);
        List<TaxonomyTerm> demographics = activeTerms(tenantId, com.brochure.cms.enums.TaxonomyType.DEMOGRAPHIC);

        List<IntakeQuestionnaireDTO.Question> questions = new ArrayList<>();
        if (!focusAreas.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "focus_areas",
                    "What brings you in?",
                    "multi",
                    IntakeCriterion.FOCUS_AREA,
                    false,
                    focusAreas.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }
        if (!modalities.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "modalities",
                    "Are there any approaches you prefer?",
                    "multi",
                    IntakeCriterion.MODALITY,
                    false,
                    modalities.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }
        if (!demographics.isEmpty()) {
            questions.add(new IntakeQuestionnaireDTO.Question(
                    "demographic",
                    "Who is the support for?",
                    "single",
                    IntakeCriterion.DEMOGRAPHIC,
                    false,
                    demographics.stream()
                            .map(t -> new IntakeQuestionnaireDTO.Option(t.getId().toString(), t.getLabel()))
                            .toList()));
        }
        questions.add(new IntakeQuestionnaireDTO.Question(
                "delivery",
                "How would you prefer to meet?",
                "single",
                IntakeCriterion.SERVICE_DELIVERY,
                false,
                List.of(
                        new IntakeQuestionnaireDTO.Option("VIRTUAL", "Virtual"),
                        new IntakeQuestionnaireDTO.Option("IN_PERSON", "In-person"),
                        new IntakeQuestionnaireDTO.Option("HYBRID", "No preference"))));

        return new IntakeQuestionnaireDTO.Questionnaire(
                "Find the right therapist",
                "Answer a few anonymous questions and we’ll suggest therapists who may be a good fit.",
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
