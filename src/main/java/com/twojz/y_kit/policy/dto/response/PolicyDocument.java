package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "제출 서류")
public class PolicyDocument {
    @Schema(description = "원본 서류 내용")
    private String documentsOriginal;

    @Schema(description = "파싱된 서류 목록")
    private List<String> documentsParsed;

    public static PolicyDocument from(PolicyDocumentEntity entity) {
        if(entity == null) { return null; }
         return PolicyDocument.builder()
                .documentsOriginal(entity.getDocumentsOriginal())
                .build();
    }
}
