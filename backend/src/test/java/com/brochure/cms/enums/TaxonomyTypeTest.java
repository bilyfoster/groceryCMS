package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaxonomyTypeTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        TaxonomyType[] values = TaxonomyType.values();
        assertEquals(3, values.length, "TaxonomyType should have exactly 3 values");
        assertEquals(TaxonomyType.ALLERGY_TYPE, values[0]);
        assertEquals(TaxonomyType.DIET_TYPE, values[1]);
        assertEquals(TaxonomyType.PRODUCT_CATEGORY, values[2]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(TaxonomyType.ALLERGY_TYPE, TaxonomyType.valueOf("ALLERGY_TYPE"));
        assertEquals(TaxonomyType.DIET_TYPE, TaxonomyType.valueOf("DIET_TYPE"));
        assertEquals(TaxonomyType.PRODUCT_CATEGORY, TaxonomyType.valueOf("PRODUCT_CATEGORY"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> TaxonomyType.valueOf("UNKNOWN"));
    }
}
