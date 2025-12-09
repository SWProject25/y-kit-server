package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자격 요건")
public class PolicyQualification {
    @Schema(description = "연령 제한 여부", example = "Y")
    private String ageLimitYn;

    @Schema(description = "최소 연령", example = "18")
    private Integer minAge;

    @Schema(description = "최대 연령", example = "34")
    private Integer maxAge;

    @Schema(description = "소득 조건 구분", example = "BELOW_MEDIAN")
    private String incomeConditionType;

    @Schema(description = "최소 소득", example = "0")
    private BigDecimal minIncome;

    @Schema(description = "최대 소득", example = "5000000")
    private BigDecimal maxIncome;

    @Schema(description = "소득 기타 내용")
    private String incomeEtc;

    @Schema(description = "결혼 상태", example = "ALL")
    private String maritalStatus;

    @Schema(description = "학력 요건", example = "UNIVERSITY")
    private String educationLevel;

    @Schema(description = "취업 상태", example = "UNEMPLOYED")
    private String employmentStatus;

    @Schema(description = "전공 분야", example = "ALL")
    private String majorField;

    @Schema(description = "특화 요건", example = "STARTUP")
    private String specializedRequirement;

    @Schema(description = "추가 신청 자격 조건")
    private String additionalQualification;

    public static PolicyQualification from(PolicyQualificationEntity entity) {
        return PolicyQualification.builder()
                .ageLimitYn(entity.getSprtTrgtAgeLmtYn())
                .minAge(entity.getSprtTrgtMinAge())
                .maxAge(entity.getSprtTrgtMaxAge())
                .incomeConditionType(entity.getEarnCndSeCd() != null ? entity.getEarnCndSeCd().getDescription() : null)
                .minIncome(entity.getEarnMinAmt())
                .maxIncome(entity.getEarnMaxAmt())
                .incomeEtc(entity.getEarnEtcCn())
                .maritalStatus(entity.getMrgSttsCd() != null ? entity.getMrgSttsCd().getDescription() : null)
                .educationLevel(entity.getSchoolCd() != null ? entity.getSchoolCd().getDescription() : null)
                .employmentStatus(entity.getJobCd() != null ? entity.getJobCd().getDescription() : null)
                .majorField(entity.getPlcyMajorCd() != null ? entity.getPlcyMajorCd().getDescription() : null)
                .specializedRequirement(entity.getSbizCd() != null ? entity.getSbizCd().getDescription() : null)
                .additionalQualification(entity.getAddAplyQlfcCndCn())
                .build();
    }
}
