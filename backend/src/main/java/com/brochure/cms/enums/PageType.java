package com.brochure.cms.enums;

public enum PageType {
    HOME,
    BLOG,
    FAQ,
    CONTACT,
    STAFF,
    GALLERY,
    SERVICE,
    CUSTOM,
    NOT_FOUND;

    public static PageType fromDb(String value) {
        if (value == null || value.isBlank()) {
            return CUSTOM;
        }
        if ("404".equals(value)) {
            return NOT_FOUND;
        }
        return PageType.valueOf(value.toUpperCase().replace('-', '_'));
    }

    public String toDb() {
        if (this == NOT_FOUND) {
            return "404";
        }
        return name().toLowerCase().replace('_', '-');
    }
}
