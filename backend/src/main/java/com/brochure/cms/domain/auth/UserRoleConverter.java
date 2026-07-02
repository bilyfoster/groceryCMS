package com.brochure.cms.domain.auth;

import com.brochure.cms.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute != null ? attribute.toDb() : UserRole.VIEWER.toDb();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return UserRole.fromDb(dbData);
    }
}
