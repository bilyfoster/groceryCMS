package com.brochure.cms.enums;

/**
 * Categories of admin-managed taxonomy used to classify grocery products.
 *
 * <p>Terms within each type (e.g. "Gluten", "Vegan", "Bakery") are stored as
 * {@code TaxonomyTerm} rows and managed by administrators without developer
 * involvement. The fixed set of <em>types</em> lives here because the recommendation
 * engine and directory filters depend on their stable semantics.
 */
public enum TaxonomyType {

    /** Allergens or ingredients the product is free of (e.g. Gluten, Dairy, Nuts). */
    ALLERGY_TYPE,

    /** Dietary lifestyle tags (e.g. Vegan, Keto, Paleo, Organic). */
    DIET_TYPE,

    /** Product category or aisle (e.g. Bakery, Pantry, Frozen, Produce). */
    PRODUCT_CATEGORY;
}
