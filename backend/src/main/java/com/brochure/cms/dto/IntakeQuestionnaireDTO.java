package com.brochure.cms.dto;

import java.util.List;
import java.util.Map;

/**
 * Configurable intake questionnaire and the visitor's answers.
 */
public class IntakeQuestionnaireDTO {

    public record Questionnaire(
            String title,
            String description,
            List<Question> questions) {}

    public record Question(
            String id,
            String label,
            String type,
            IntakeCriterion criterion,
            boolean required,
            List<Option> options) {}

    public record Option(
            String value,
            String label) {}

    public record MatchRequest(
            Map<String, List<String>> answers) {}
}
