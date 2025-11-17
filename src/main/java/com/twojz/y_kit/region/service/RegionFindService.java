package com.twojz.y_kit.region.service;

import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionFindService {
    private final RegionRepository regionRepository;

    public Region findRegion(String name) {
        return regionRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
    }
}
