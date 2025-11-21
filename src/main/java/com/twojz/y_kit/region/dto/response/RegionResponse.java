package com.twojz.y_kit.region.dto.response;

import com.twojz.y_kit.region.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionResponse {
    @Schema(description = "지역 코드", example = "11000")
    private String code;

    @Schema(description = "지역명", example = "서울특별시")
    private String name;

    public static RegionResponse from(Region region) {
        return new RegionResponse(
                region.getCode(),
                region.getName()
        );
    }
}
