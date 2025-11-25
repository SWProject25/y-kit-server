package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "정책 상세 정보")
public class PolicyDetail {
    @Schema(description = "정책명", example = "청년 취업 지원 사업")
    private String policyName;

    @Schema(description = "정책 설명")
    private String description;

    @Schema(description = "주관 기관명", example = "고용노동부")
    private String supervisingInstitution;

    @Schema(description = "운영 기관명", example = "한국고용정보원")
    private String operatingInstitution;

    @Schema(description = "정책 승인 상태", example = "APPROVED")
    private String approvalStatus;

    @Schema(description = "정책 제공 방법", example = "ONLINE")
    private String provisionMethod;

    @Schema(description = "정책 지원 내용")
    private String supportContent;

    @Schema(description = "지원 규모", example = "1000명")
    private String supportScale;

    @Schema(description = "참여 제안 대상")
    private String participationTarget;

    @Schema(description = "심사 방법")
    private String screeningMethod;

    @Schema(description = "사업 기간 구분", example = "SPECIFIC_PERIOD")
    private String businessPeriodType;

    @Schema(description = "사업 시작일", example = "2024-01-01")
    private LocalDate businessStartDate;

    @Schema(description = "사업 종료일", example = "2024-12-31")
    private LocalDate businessEndDate;

    @Schema(description = "사업 기간 기타 내용")
    private String businessPeriodEtc;

    @Schema(description = "기타 사항")
    private String etcMatters;

    @Schema(description = "참고 URL 1")
    private String referenceUrl1;

    @Schema(description = "참고 URL 2")
    private String referenceUrl2;

    public static PolicyDetail from(PolicyDetailEntity detail) {
        return PolicyDetail.builder()
                .policyName(detail.getPlcyNm())
                .description(detail.getPlcyExplnCn())
                .supervisingInstitution(detail.getSprvsnInstCdNm())
                .operatingInstitution(detail.getOperInstCdNm())
                .approvalStatus(detail.getPlcyAprvSttsCd() != null ? detail.getPlcyAprvSttsCd().name() : null)
                .provisionMethod(detail.getPlcyPvsnMthdCd() != null ? detail.getPlcyPvsnMthdCd().name() : null)
                .supportContent(detail.getPlcySprtCn())
                .supportScale(detail.getSprtSclCnt())
                .participationTarget(detail.getPtcpPrpTrgtCn())
                .screeningMethod(detail.getSrngMthdCn())
                .businessPeriodType(detail.getBizPrdSeCd() != null ? detail.getBizPrdSeCd().name() : null)
                .businessStartDate(detail.getBizPrdBgngYmd())
                .businessEndDate(detail.getBizPrdEndYmd())
                .businessPeriodEtc(detail.getBizPrdEtcCn())
                .etcMatters(detail.getEtcMttrCn())
                .referenceUrl1(detail.getRefUrlAddr1())
                .referenceUrl2(detail.getRefUrlAddr2())
                .build();
    }
}
