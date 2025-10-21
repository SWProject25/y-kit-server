package com.twojz.y_kit.region.controller;

import com.twojz.y_kit.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RegionAdminController {
    private final RegionService regionService;

    @GetMapping("/v1/region/init")
    public ResponseEntity<Void> initRegions() {
        regionService.initRegions();
        return ResponseEntity.noContent().build();
    }
}
