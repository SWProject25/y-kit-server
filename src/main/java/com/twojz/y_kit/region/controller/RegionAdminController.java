package com.twojz.y_kit.region.controller;

import com.twojz.y_kit.region.service.RegionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "법정동 코드 동기화 API")
@RequiredArgsConstructor
public class RegionAdminController {
    private final RegionService regionService;

    @GetMapping("/v1/region/init")
    public ResponseEntity<Void> initRegions() {
        regionService.initRegions();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/init/{level}")
    public ResponseEntity<String> initRegionByLevel(@PathVariable String level) {
        int count = switch (level.toLowerCase()) {
            case "sido" -> regionService.initSido();
            case "sigungu" -> regionService.initSigungu();
            case "dong" -> regionService.initDong();
            case "ree" -> regionService.initRee();
            default -> throw new IllegalArgumentException("잘못된 지역 레벨: " + level);
        };

        return ResponseEntity.ok(getLevelName(level) + " " + count + "건 저장 완료");
    }

    private String getLevelName(String level) {
        return switch (level.toLowerCase()) {
            case "sido" -> "시도";
            case "sigungu" -> "시군구";
            case "dong" -> "읍면동";
            case "ree" -> "리";
            default -> level;
        };
    }

}
