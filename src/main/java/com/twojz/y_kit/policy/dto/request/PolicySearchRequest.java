package com.twojz.y_kit.policy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "정책 목록 검색 요청 DTO")
public class PolicySearchRequest {

    @Schema(description = "검색 키워드 (정책명, 내용, 태그 등)", example = "청년")
    private String keyword;

    @Schema(description = "카테고리 ID", example = "3")
    private Long categoryId;

    @Schema(description = "지역 코드", example = "11")
    private String regionCode;

    @Schema(description = "나이 필터", example = "25")
    private Integer age;

    @Schema(description = "신청 가능 여부", example = "true")
    private Boolean isApplicationAvailable;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page;

    @Schema(description = "페이지 크기", example = "10")
    private Integer size;

    @Schema(description = "정렬 기준 (예: createdAt,viewCount)", example = "createdAt,desc")
    private String sort;
}