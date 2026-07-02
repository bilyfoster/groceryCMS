package com.brochure.cms.domain.auth;

import com.brochure.cms.enums.AuthTokenPurpose;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AuthTokenPurposeConverter implements AttributeConverter<AuthTokenPurpose, String> {

    @Override
    public String convertToDatabaseColumn(AuthTokenPurpose attribute) {
        return attribute != null ? attribute.toDb() : null;
    }

    @Override
    public AuthTokenPurpose convertToEntityAttribute(String dbData) {
        return AuthTokenPurpose.fromDb(dbData);
    }
}
