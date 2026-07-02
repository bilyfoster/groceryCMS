package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServiceDeliveryTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllEnumValuesReturned() {
        ServiceDelivery[] values = ServiceDelivery.values();
        assertEquals(3, values.length, "ServiceDelivery should have exactly 3 values");
        assertEquals(ServiceDelivery.VIRTUAL, values[0]);
        assertEquals(ServiceDelivery.IN_PERSON, values[1]);
        assertEquals(ServiceDelivery.HYBRID, values[2]);
    }

    @Test
    void valueOf_When_ValidEnumNameProvided_Expect_CorrectEnumReturned() {
        assertEquals(ServiceDelivery.VIRTUAL, ServiceDelivery.valueOf("VIRTUAL"));
        assertEquals(ServiceDelivery.IN_PERSON, ServiceDelivery.valueOf("IN_PERSON"));
        assertEquals(ServiceDelivery.HYBRID, ServiceDelivery.valueOf("HYBRID"));
    }

    @Test
    void valueOf_When_UnknownNameProvided_Expect_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ServiceDelivery.valueOf("UNKNOWN"));
    }
}
