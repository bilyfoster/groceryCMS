package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StoreSectionTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        StoreSection[] values = StoreSection.values();
        assertEquals(6, values.length, "StoreSection should have exactly 6 values");
        assertEquals(StoreSection.PANTRY, values[0]);
        assertEquals(StoreSection.BAKERY, values[1]);
        assertEquals(StoreSection.REFRIGERATED, values[2]);
        assertEquals(StoreSection.FROZEN, values[3]);
        assertEquals(StoreSection.PRODUCE, values[4]);
        assertEquals(StoreSection.FRONT, values[5]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(StoreSection.PANTRY, StoreSection.valueOf("PANTRY"));
        assertEquals(StoreSection.BAKERY, StoreSection.valueOf("BAKERY"));
        assertEquals(StoreSection.REFRIGERATED, StoreSection.valueOf("REFRIGERATED"));
        assertEquals(StoreSection.FROZEN, StoreSection.valueOf("FROZEN"));
        assertEquals(StoreSection.PRODUCE, StoreSection.valueOf("PRODUCE"));
        assertEquals(StoreSection.FRONT, StoreSection.valueOf("FRONT"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StoreSection.valueOf("UNKNOWN"));
    }
}
