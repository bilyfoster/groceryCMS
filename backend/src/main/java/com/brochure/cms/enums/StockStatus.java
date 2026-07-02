package com.brochure.cms.enums;

/**
 * Current stock availability of a grocery product.
 *
 * <p>Persisted as a string ({@code @Enumerated(EnumType.STRING)}) because the
 * fixed values have stable semantics used by the directory, recommendation engine,
 * and stock badge UI.
 */
public enum StockStatus {

    /** Product is readily available on shelves. */
    IN_STOCK,

    /** Product is running low and may sell out soon. */
    LOW_STOCK,

    /** Product is temporarily out of stock. */
    OUT_OF_STOCK,

    /** Product is no longer carried. */
    DISCONTINUED;
}
