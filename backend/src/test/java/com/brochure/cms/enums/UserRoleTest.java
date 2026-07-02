package com.brochure.cms.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserRoleTest {

    @Test
    void values_When_EnumIsCalled_Expect_AllRolesIncludingTherapist() {
        UserRole[] values = UserRole.values();
        assertEquals(4, values.length, "UserRole should have exactly 4 values");
        assertEquals(UserRole.THERAPIST, UserRole.valueOf("THERAPIST"));
    }

    @Test
    void fromDb_When_NullProvided_Expect_ViewerDefault() {
        assertEquals(UserRole.VIEWER, UserRole.fromDb(null));
    }

    @Test
    void fromDb_When_LowercaseProvided_Expect_CorrectRole() {
        assertEquals(UserRole.THERAPIST, UserRole.fromDb("therapist"));
        assertEquals(UserRole.ADMIN, UserRole.fromDb("admin"));
    }

    @Test
    void toDb_When_Called_Expect_LowercaseName() {
        assertEquals("therapist", UserRole.THERAPIST.toDb());
        assertEquals("editor", UserRole.EDITOR.toDb());
    }
}
