package com.brochure.cms.enums;

public enum AuthTokenPurpose {
    MAGIC_LINK,
    PASSWORD_RESET,
    VERIFY_EMAIL;

    public static AuthTokenPurpose fromDb(String value) {
        return AuthTokenPurpose.valueOf(value.toUpperCase().replace('-', '_'));
    }

    public String toDb() {
        return name().toLowerCase();
    }
}
