package com.brochure.cms.dto;

import jakarta.validation.constraints.Email;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public intake questionnaire payload used by the product recommendation engine.
 *
 * <p>Captures shopper dietary preferences and symptom concerns only — no medical
 * diagnosis or PHI. All fields are optional, but selecting at least one avoided
 * allergen or dietary preference is recommended for useful results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeRequestDTO {

    /** Allergy taxonomy term ids the shopper wants to avoid. */
    private Set<UUID> avoidedAllergies;

    /** Diet taxonomy term ids the shopper prefers (e.g. vegan, keto). */
    private Set<UUID> preferredDiets;

    /** Product category taxonomy term ids the shopper is interested in. */
    private Set<UUID> preferredCategories;

    /** Symptom question values selected by the shopper. */
    private Set<String> symptoms;

    /** Opt-in contact email; validated when supplied. */
    @Email(message = "contactEmail must be a valid email address")
    private String contactEmail;
}
