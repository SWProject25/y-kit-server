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
public class HotDealUpdateRequest {
    @Schema(description = "핫딜 제목", example = "파리바게뜨 빵 60% 추가 할인")
    private String title;

    @Schema(description = "상세 설명", example = "갤럭시 S24 반값 세일! 선착순 100명")
    private String content;

    @Schema(description = "가게 이름", example = "파리바게뜨 마포점")
    private String placeName;

    @Schema(description = "주소", example = "서울 마포구 양화로 100")
    private String address;

    @NotNull(message = "딜 타입은 필수입니다.")
    @Schema(description = "딜 타입", example = "DISCOUNT")
    private DealType dealType;

    @NotNull(message = "카테고리는 필수입니다.")
    @Schema(description = "카테고리", example = "ELECTRONICS")
    private HotDealCategory category;

    @Schema(description = "지역 코드", example = "11010")
    private String regionCode;

    @Schema(description = "이미지 URL", example = "https://example.com/new-image.png")
    private String url;

    @Schema(description = "위도", example = "37.5662952")
    private Double latitude;

    @Schema(description = "경도", example = "126.9779451")
    private Double longitude;

    @Schema(description = "만료 시간", example = "2025-01-20T23:00:00")
    private LocalDateTime expiresAt;
}
