package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "지역 정보")
public class RegionInfo {
    @Schema(description = "지역 코드", example = "11")
    private String regionCode;

    @Schema(description = "지역명", example = "서울특별시")
    private String regionName;

    public static List<RegionInfo> from(List<PolicyRegion> regions) {
        return regions.stream()
                .map(r -> RegionInfo.builder()
                        .regionCode(r.getRegion().getCode())
                        .regionName(r.getRegion().getName())
                        .build())
                .toList();
    }
}
