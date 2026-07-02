package com.brochure.cms.dto;

/**
 * Dimensions a configurable intake question can map to. Each criterion corresponds
 * to a product classification or shopper symptom used by the recommendation engine.
 */
public enum IntakeCriterion {
    ALLERGY_TYPE,
    DIET_TYPE,
    PRODUCT_CATEGORY,
    SYMPTOM,
    STORE_SECTION
}
