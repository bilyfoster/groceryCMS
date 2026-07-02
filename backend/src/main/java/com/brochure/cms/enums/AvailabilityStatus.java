package com.brochure.cms.enums;

/**
 * A therapist's current availability for accepting new clients.
 *
 * <p>Persisted as a string ({@code @Enumerated(EnumType.STRING)}) because the
 * fixed values have stable semantics used by the directory, matching engine,
 * and availability badge UI.
 */
public enum AvailabilityStatus {

    /** Actively accepting new clients. */
    ACCEPTING,

    /** Accepting a limited number of new clients. */
    LIMITED,

    /** New clients are being added to a waitlist. */
    WAITLIST,

    /** Not currently accepting new clients. */
    NOT_ACCEPTING;
}
