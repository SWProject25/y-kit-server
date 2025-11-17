package com.twojz.y_kit.hotdeal.dto.request;

import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "핫딜 제목", example = "파리바게트 빵 50% 할인")
    private String title;

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

    @Schema(description = "핫딜 타입", example = "DISCOUNT")
    private DealType dealType;

    @Schema(description = "지역 코드", example = "11010")
    private String regionCode;

    @Schema(description = "만료 시간", example = "2025-01-15T18:00:00")
    private LocalDateTime expiresAt;
}
