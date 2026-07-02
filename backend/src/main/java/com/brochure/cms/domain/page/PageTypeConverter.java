package com.brochure.cms.domain.page;

import com.brochure.cms.enums.PageType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PageTypeConverter implements AttributeConverter<PageType, String> {

    @Override
    public String convertToDatabaseColumn(PageType attribute) {
        return attribute != null ? attribute.toDb() : PageType.CUSTOM.toDb();
    }

    @Override
    public PageType convertToEntityAttribute(String dbData) {
        return PageType.fromDb(dbData);
    }
}
