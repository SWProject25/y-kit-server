package com.twojz.y_kit.region.service;

import com.twojz.y_kit.region.dto.response.RegionResponse;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.entity.RegionLevel;
import com.twojz.y_kit.region.repository.RegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionFindService {
    private final RegionRepository regionRepository;

    public Region findRegionName(String name) {
        return regionRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
    }

    public Region findRegionCode(String code) {
        return regionRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
    }

    public List<RegionResponse> findSido() {
        return regionRepository.findByLevel(RegionLevel.SIDO)
                .stream()
                .map(RegionResponse::from)
                .toList();
    }

    public List<RegionResponse> findSigungu(String sidoCode) {
        return findChildrenByParentAndLevel(sidoCode, RegionLevel.SIGUNGU);
    }

    public List<RegionResponse> findDong(String sigunguCode) {
        return findChildrenByParentAndLevel(sigunguCode, RegionLevel.DONG);
    }

    private List<RegionResponse> findChildrenByParentAndLevel(String parentCode, RegionLevel level) {
        return regionRepository.findByParent_CodeAndLevel(parentCode, level)
                .stream()
                .map(RegionResponse::from)
                .toList();
    }

}
