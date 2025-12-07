package com.twojz.y_kit.policy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PolicyListResponse {
    @Schema(description = "정책 ID", example = "1")
    private Long policyId;

    @Schema(description = "정책 번호", example = "R2024012345678")
    private String policyNo;

    @Schema(description = "정책명", example = "청년 취업 지원 사업")
    private String policyName;

    @Schema(description = "정책 요약 설명", example = "청년들의 취업을 지원하는 사업입니다")
    private String summary;

    @Schema(description = "대분류 카테고리", example = "일자리")
    private String largeCategory;

    @Schema(description = "중분류 카테고리", example = "취업지원")
    private String mediumCategory;

    @Schema(description = "신청 가능 여부")
    private Boolean isApplicationAvailable;

    @Schema(description = "신청 시작일", example = "2024-01-01")
    private LocalDate applicationStartDate;

    @Schema(description = "신청 종료일", example = "2024-12-31")
    private LocalDate applicationEndDate;

    @Schema(description = "주관 기관명", example = "고용노동부")
    private String supervisingInstitution;

    @Schema(description = "지원 대상 최소 연령", example = "18")
    private Integer minAge;

    @Schema(description = "지원 대상 최대 연령", example = "34")
    private Integer maxAge;

    @Schema(description = "키워드 목록")
    private List<String> keywords;

    @Schema(description = "적용 지역 목록")
    private List<String> regions;

    @Schema(description = "조회수", example = "1234")
    private Integer viewCount;

    @Schema(description = "북마크 수", example = "56")
    private Integer bookmarkCount;

    @Schema(description = "신청 수", example = "789")
    private Integer applicationCount;

    @Schema(description = "북마크 여부")
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    @Schema(description = "등록일시")
    private LocalDateTime createdAt;
}

