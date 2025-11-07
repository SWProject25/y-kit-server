package com.twojz.y_kit.policy.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AiAnalysisConverter implements AttributeConverter<AiAnalysis, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AiAnalysis attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting AiAnalysis to JSON", e);
        }
    }

    @Override
    public AiAnalysis convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, AiAnalysis.class);
        } catch (Exception e) {
            throw new IllegalStateException("Error reading JSON to AiAnalysis", e);
        }
    }
}
