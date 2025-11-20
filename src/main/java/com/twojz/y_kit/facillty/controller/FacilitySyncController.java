package com.twojz.y_kit.facillty.controller;

import com.twojz.y_kit.facillty.service.FacilitySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class FacilitySyncController {

    private final FacilitySyncService syncService;

    @GetMapping("/sync/facility")
    public String sync() {
        syncService.fetchAllFacilitiesAsync();
        return "Started async facility sync!";
    }
}
