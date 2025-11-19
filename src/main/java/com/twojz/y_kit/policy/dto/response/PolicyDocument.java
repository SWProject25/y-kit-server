package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "제출 서류")
public class PolicyDocument {
    @Schema(description = "원본 서류 내용")
    private String documentsOriginal;

    @Schema(description = "파싱된 서류")
    private DocumentParsed documentsParsed;

    public static PolicyDocument from(PolicyDocumentEntity entity) {
        if (entity == null) return null;

        return PolicyDocument.builder()
                .documentsOriginal(entity.getDocumentsOriginal())
                .documentsParsed(entity.getDocumentsParsed())
                .build();
    }
}
