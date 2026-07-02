package com.brochure.cms.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BlockType {
    HERO,
    TEXT,
    CTA,
    IMAGE,
    VIDEO,
    EMBED,
    GROUP,
    COLUMNS,
    SPACER,
    DIVIDER,
    QUOTE,
    TABLE,
    ACCORDION,
    BUTTON,
    ICON_TEXT,
    TESTIMONIALS,
    PRICING,
    MAP;

    @JsonCreator
    public static BlockType fromDb(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Block type required");
        }
        return BlockType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toDb() {
        return name().toLowerCase();
    }
}
