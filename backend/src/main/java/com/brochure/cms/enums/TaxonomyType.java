package com.brochure.cms.enums;

/**
 * Categories of admin-managed taxonomy used to classify therapists.
 *
 * <p>Terms within each type (e.g. "Anxiety", "EMDR") are stored as
 * {@code TaxonomyTerm} rows and managed by administrators without developer
 * involvement. The fixed set of <em>types</em> lives here because the matching
 * engine and directory filters depend on their stable semantics.
 */
public enum TaxonomyType {

    /** Clinical focus areas / specialties (e.g. Anxiety, Trauma, Grief). */
    FOCUS_AREA,

    /** Therapy modalities / methods (e.g. CBT, DBT, EMDR). */
    MODALITY,

    /** Client demographics served (e.g. Adults, Adolescents, Couples). */
    DEMOGRAPHIC;
}
