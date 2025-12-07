package com.twojz.y_kit.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class GroupPurchaseCreateRequest {
    @Schema(description = "공동 구매 제목", example = "한정판 티셔츠 공동 구매")
    private String title;

    @Schema(description = "공동 구매 내용")
    private String content;

    @Schema(description = "상품명", example = "Limited Edition T-shirt")
    private String productName;

    @Schema(description = "상품 링크", example = "https://example.com/product/123")
    private String productLink;

    @Schema(description = "연락 수단", example = "연락처 또는 오픈채팅방 주소")
    private String contact;

    @Schema(description = "상품 가격", example = "25000")
    private BigDecimal price;

    @Schema(description = "최소 참여 인원", example = "5")
    private int minParticipants;

    @Schema(description = "최대 참여 인원", example = "20")
    private int maxParticipants;

    @Schema(description = "모집 마감일", example = "2025-12-31")
    private LocalDate deadline;

    @Schema(description = "위도", example = "37.5662952")
    private Double latitude;

    @Schema(description = "경도", example = "126.9779451")
    private Double longitude;

    @Schema(description = "주소", example = "서울 마포구 와우산로 11길 45")
    private String address;

    @Schema(description = "시/도 (주소 기반 자동 매핑 시)", example = "서울특별시")
    private String sido;

    @Schema(description = "시/군/구 (주소 기반 자동 매핑 시)", example = "마포구")
    private String sigungu;

    @Schema(description = "읍/면/동 (주소 기반 자동 매핑 시)", example = "서교동")
    private String dong;

    @Schema(description = "카테고리", example = "FOOD")
    private String category;
}