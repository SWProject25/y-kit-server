package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "AI 분석 정보")
public class AiAnalysisInfo {
    @Schema(description = "AI 요약")
    private String summary;

    @Schema(description = "장점")
    private String  pros;

    @Schema(description = "단점")
    private String corn;

    @Schema(description = "생성 일시")
    private LocalDateTime generatedAt;

    public static AiAnalysisInfo from(PolicyEntity entity) {
        if (entity.getAiAnalysis() == null) return null;
        return AiAnalysisInfo.builder()
                .summary(entity.getAiAnalysis().getSummary())
                .pros(entity.getAiAnalysis().getPros())
                .corn(entity.getAiAnalysis().getCons())
                .generatedAt(entity.getAiGeneratedAt())
                .build();
    }
}
