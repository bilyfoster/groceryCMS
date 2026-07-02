package com.brochure.cms.enums;

/**
 * How a therapist delivers sessions.
 *
 * <p>Persisted as a string ({@code @Enumerated(EnumType.STRING)}) because the
 * fixed values have stable semantics used by the directory filters and the
 * matching engine.
 */
public enum ServiceDelivery {

    /** Sessions are conducted remotely (telehealth). */
    VIRTUAL,

    /** Sessions are conducted in person at a practice location. */
    IN_PERSON,

    /** Therapist offers both virtual and in-person sessions. */
    HYBRID;
}
