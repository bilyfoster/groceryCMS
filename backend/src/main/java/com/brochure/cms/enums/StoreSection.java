package com.brochure.cms.enums;

/**
 * Store area or department where a product is located.
 *
 * <p>Persisted as a string ({@code @Enumerated(EnumType.STRING)}) because the
 * fixed values have stable semantics used by directory filters and the
 * recommendation engine.
 */
public enum StoreSection {

    /** Dry goods, baking supplies, cereals, etc. */
    PANTRY,

    /** Bread, tortillas, pastries, and other baked items. */
    BAKERY,

    /** Refrigerated items requiring cold storage. */
    REFRIGERATED,

    /** Frozen items. */
    FROZEN,

    /** Fresh fruits, vegetables, and produce. */
    PRODUCE,

    /** Items near the front registers or checkout area. */
    FRONT;
}
