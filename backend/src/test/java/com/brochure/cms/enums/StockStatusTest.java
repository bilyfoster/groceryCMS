package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StockStatusTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        StockStatus[] values = StockStatus.values();
        assertEquals(4, values.length, "StockStatus should have exactly 4 values");
        assertEquals(StockStatus.IN_STOCK, values[0]);
        assertEquals(StockStatus.LOW_STOCK, values[1]);
        assertEquals(StockStatus.OUT_OF_STOCK, values[2]);
        assertEquals(StockStatus.DISCONTINUED, values[3]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(StockStatus.IN_STOCK, StockStatus.valueOf("IN_STOCK"));
        assertEquals(StockStatus.LOW_STOCK, StockStatus.valueOf("LOW_STOCK"));
        assertEquals(StockStatus.OUT_OF_STOCK, StockStatus.valueOf("OUT_OF_STOCK"));
        assertEquals(StockStatus.DISCONTINUED, StockStatus.valueOf("DISCONTINUED"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StockStatus.valueOf("UNKNOWN"));
    }
}
