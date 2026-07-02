package com.brochure.cms.domain.page;

import com.brochure.cms.enums.BlockType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BlockTypeConverter implements AttributeConverter<BlockType, String> {

    @Override
    public String convertToDatabaseColumn(BlockType attribute) {
        return attribute != null ? attribute.toDb() : null;
    }

    @Override
    public BlockType convertToEntityAttribute(String dbData) {
        return BlockType.fromDb(dbData);
    }
}
