package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "AI 분석 정보")
public class AiAnalysisInfo {
    @Schema(description = "AI 요약")
    private String summary;

    @Schema(description = "장점")
    private List<String> advantages;

    @Schema(description = "단점")
    private List<String> disadvantages;

    @Schema(description = "생성 일시")
    private LocalDateTime generatedAt;

    public static AiAnalysisInfo from(PolicyEntity entity) {
        if (entity.getAiAnalysis() == null) return null;
        return AiAnalysisInfo.builder()
                .summary(entity.getAiAnalysis().getSummary())
                .advantages(entity.getAiAnalysis().getAdvantages())
                .disadvantages(entity.getAiAnalysis().getDisadvantages())
                .generatedAt(entity.getAiGeneratedAt())
                .build();
    }
}
