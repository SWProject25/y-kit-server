package com.twojz.y_kit.facillty.dto.response;

import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacilityMapResponse {
    @Schema(description = "시설 ID", example = "1243")
    private Long id;

    @Schema(description = "시설 이름", example = "서울 체육 센터")
    private String name;

    @Schema(description = "위도(latitude)", example = "37.5665")
    private double latitude;

    @Schema(description = "경도(longitude)", example = "126.9780")
    private double longitude;

    @Schema(description = "주소", example = "서울특별시 중구 세종대로 110")
    private String address;

    @Schema(description = "시설 이미지 URL", example = "https://example.com/image.jpg")
    private String imgUrl;

    @Schema(description = "카테고리", example = "SPORTS")
    private String category;

    public static FacilityMapResponse from(FacilityEntity e) {
        return new FacilityMapResponse(
                e.getId(),
                e.getName(),
                e.getLatitude(),
                e.getLongitude(),
                e.getAddress(),
                e.getImgUrl(),
                e.getCategory() != null ? e.getCategory().name() : null
        );
    }
}
