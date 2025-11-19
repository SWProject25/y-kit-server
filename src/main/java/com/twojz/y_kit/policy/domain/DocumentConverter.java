package com.twojz.y_kit.policy.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DocumentConverter implements AttributeConverter<DocumentParsed, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(DocumentParsed attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting DocumentParsed to JSON", e);
        }
    }

    @Override
    public DocumentParsed convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, DocumentParsed.class);
        } catch (Exception e) {
            throw new IllegalStateException("Error reading JSON to DocumentParsed", e);
        }
    }
}
