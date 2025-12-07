package com.twojz.y_kit.hotdeal.dto.request;

import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HotDealCreateRequest {
    @NotNull(message = "제목은 필수입니다.")
    @Schema(description = "핫딜 제목", example = "파리바게트 빵 50% 할인")
    private String title;

    @Schema(description = "상세 설명", example = "갤럭시 S24 반값 세일! 선착순 100명")
    private String content;

    @Schema(description = "가게 이름", example = "파리바게뜨 마포점")
    private String placeName;

    @Schema(description = "위도", example = "37.5662952")
    private Double latitude;

    @Schema(description = "경도", example = "126.9779451")
    private Double longitude;

    @Schema(description = "가게 주소", example = "서울 마포구 와우산로 11길 45")
    private String address;

    @Schema(description = "이미지 URL", example = "https://example.com/hotdeal.png")
    private String url;

    @NotNull(message = "딜 타입은 필수입니다.")
    @Schema(description = "딜 타입", example = "DISCOUNT")
    private DealType dealType;

    @NotNull(message = "카테고리는 필수입니다.")
    @Schema(description = "카테고리", example = "ELECTRONICS")
    private HotDealCategory category;

    @Schema(description = "시/도 (주소 기반 자동 매핑 시)", example = "서울특별시")
    private String sido;

    @Schema(description = "시/군/구 (주소 기반 자동 매핑 시)", example = "마포구")
    private String sigungu;

    @Schema(description = "읍/면/동 (주소 기반 자동 매핑 시)", example = "서교동")
    private String dong;

    @Schema(description = "만료 시간", example = "2025-01-15T18:00:00")
    private LocalDateTime expiresAt;
}
