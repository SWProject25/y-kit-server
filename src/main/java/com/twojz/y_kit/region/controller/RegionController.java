package com.twojz.y_kit.region.controller;

import com.twojz.y_kit.region.dto.response.RegionResponse;
import com.twojz.y_kit.region.service.RegionFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지역 API")
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionController {
    private final RegionFindService regionFindService;

    @Operation(summary = "시/도 목록 조회")
    @GetMapping("/sido")
    public List<RegionResponse> getSidoList() {
        return regionFindService.findSido();
    }

    @Operation(summary = "시군구 목록 조회")
    @GetMapping("/sigungu")
    public List<RegionResponse> getSigunguList(@RequestParam String sidoCode) {
        return regionFindService.findSigungu(sidoCode);
    }

    @Operation(summary = "읍면동 목록 조회")
    @GetMapping("/dong")
    public List<RegionResponse> getDongList(@RequestParam String sigunguCode) {
        return regionFindService.findDong(sigunguCode);
    }
}
