package com.twojz.y_kit.facillty.controller;

import com.twojz.y_kit.facillty.dto.response.FacilityMapResponse;
import com.twojz.y_kit.facillty.service.FacilityFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공유시설 API")
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityFindService facilityFindService;

    @Operation(summary = "지도 화면 내 시설 조회")
    @GetMapping("/map")
    public List<FacilityMapResponse> getFacilitiesOnMap(
            @Parameter(description = "지도 화면의 최소 위도", required = true)
            @RequestParam double minLat,

            @Parameter(description = "지도 화면의 최대 위도", required = true)
            @RequestParam double maxLat,

            @Parameter(description = "지도 화면의 최소 경도", required = true)
            @RequestParam double minLng,

            @Parameter(description = "지도 화면의 최대 경도", required = true)
            @RequestParam double maxLng
    ) {
        return facilityFindService.findFacilitiesInBounds(minLat, maxLat, minLng, maxLng);
    }
}
