package com.brochure.cms.enums;

/**
 * Authorization roles for CMS users.
 *
 * <p>{@code THERAPIST} is a self-service role that may edit only its own
 * therapist profile (ownership-enforced); content roles map to the PRD's
 * Content Administrator ({@code EDITOR}) and Practice Administrator
 * ({@code ADMIN}). See the engineering PRD §5.
 */
public enum UserRole {
    VIEWER,
    EDITOR,
    ADMIN,
    THERAPIST;

    public static UserRole fromDb(String value) {
        if (value == null) {
            return VIEWER;
        }
        return UserRole.valueOf(value.toUpperCase());
    }

    public String toDb() {
        return name().toLowerCase();
    }
}
