package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaxonomyTypeTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        TaxonomyType[] values = TaxonomyType.values();
        assertEquals(3, values.length, "TaxonomyType should have exactly 3 values");
        assertEquals(TaxonomyType.FOCUS_AREA, values[0]);
        assertEquals(TaxonomyType.MODALITY, values[1]);
        assertEquals(TaxonomyType.DEMOGRAPHIC, values[2]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(TaxonomyType.FOCUS_AREA, TaxonomyType.valueOf("FOCUS_AREA"));
        assertEquals(TaxonomyType.MODALITY, TaxonomyType.valueOf("MODALITY"));
        assertEquals(TaxonomyType.DEMOGRAPHIC, TaxonomyType.valueOf("DEMOGRAPHIC"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> TaxonomyType.valueOf("UNKNOWN"));
    }
}
