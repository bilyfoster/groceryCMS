package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AvailabilityStatusTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        AvailabilityStatus[] values = AvailabilityStatus.values();
        assertEquals(4, values.length, "AvailabilityStatus should have exactly 4 values");
        assertEquals(AvailabilityStatus.ACCEPTING, values[0]);
        assertEquals(AvailabilityStatus.LIMITED, values[1]);
        assertEquals(AvailabilityStatus.WAITLIST, values[2]);
        assertEquals(AvailabilityStatus.NOT_ACCEPTING, values[3]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(AvailabilityStatus.ACCEPTING, AvailabilityStatus.valueOf("ACCEPTING"));
        assertEquals(AvailabilityStatus.LIMITED, AvailabilityStatus.valueOf("LIMITED"));
        assertEquals(AvailabilityStatus.WAITLIST, AvailabilityStatus.valueOf("WAITLIST"));
        assertEquals(AvailabilityStatus.NOT_ACCEPTING, AvailabilityStatus.valueOf("NOT_ACCEPTING"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> AvailabilityStatus.valueOf("UNKNOWN"));
    }
}
