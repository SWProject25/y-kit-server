package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "신청 정보")
public class PolicyApplication {
    @Schema(description = "지원 규모 제한 여부", example = "Y")
    private String supportScaleLimit;

    @Schema(description = "선착순 여부", example = "Y")
    private String firstComeFirstServed;

    @Schema(description = "신청 기간 구분", example = "SPECIFIC_PERIOD")
    private String applicationPeriodType;

    @Schema(description = "신청 시작일", example = "2024-01-01")
    private LocalDate applicationStartDate;

    @Schema(description = "신청 종료일", example = "2024-12-31")
    private LocalDate applicationEndDate;

    @Schema(description = "신청 방법")
    private String applicationMethod;

    @Schema(description = "신청 URL")
    private String applicationUrl;

    public static PolicyApplication from(PolicyApplicationEntity entity) {
        return PolicyApplication.builder()
                .supportScaleLimit(entity.getSprtSclLmtYn())
                .firstComeFirstServed(entity.getSprtArvlSqncYn())
                .applicationPeriodType(entity.getAplyPrdSeCd() != null ? entity.getAplyPrdSeCd().getDescription() : null)
                .applicationStartDate(entity.getAplyBgngYmd())
                .applicationEndDate(entity.getAplyEndYmd())
                .applicationMethod(entity.getPlcyAplyMthdCn())
                .applicationUrl(entity.getAplyUrlAddr())
                .build();
    }
}
