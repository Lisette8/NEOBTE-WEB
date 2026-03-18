package com.sesame.neobte.Entities.Converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Oracle (<= 21c) doesn't support BOOLEAN columns in SQL DDL.
 * Persist booleans as NUMBER(1): 1=true, 0=false.
 */
@Converter(autoApply = false)
public class BooleanToIntegerConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        return Boolean.TRUE.equals(attribute) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return dbData != null && dbData.intValue() == 1;
    }
}

