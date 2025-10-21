package com.twojz.y_kit.region.service;

import com.twojz.y_kit.external.vworld.client.VWorldClient;
import com.twojz.y_kit.external.vworld.dto.VWorldApiEndpoint;
import com.twojz.y_kit.external.vworld.dto.VWorldRegionApiResponse.VWorldRegionItem;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.entity.RegionLevel;
import com.twojz.y_kit.region.repository.RegionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final VWorldClient vWorldClient;
    private final RegionRepository regionRepository;

    @Transactional
    public void initRegions() {
        if (regionRepository.count() > 0) {
            log.info("⚠️ 이미 지역 데이터가 존재합니다.");
            return;
        }

        log.info("🚀 VWorld 행정구역 데이터 초기화 시작");

        Map<String, Region> regionMap = new ConcurrentHashMap<>();
        int totalCount = 0;

        // 1. 시도
        List<VWorldRegionItem> sidoList = fetchRegions(VWorldApiEndpoint.ADM_CODE_LIST_PATH, null);
        totalCount += saveRegions(sidoList, RegionLevel.SIDO, regionMap);

        // 2. 시군구
        List<VWorldRegionItem> sigunguList = fetchRegions(VWorldApiEndpoint.ADM_SI_LIST_PATH, sidoList);
        totalCount += saveRegions(sigunguList, RegionLevel.SIGUNGU, regionMap);

        // 3. 읍면동
        List<VWorldRegionItem> dongList = fetchRegions(VWorldApiEndpoint.ADM_DONG_LIST_PATH, sigunguList);
        totalCount += saveRegions(dongList, RegionLevel.DONG, regionMap);

        // 4. 리
        List<VWorldRegionItem> reeList = fetchRegions(VWorldApiEndpoint.ADM_REE_LIST_PATH, dongList);
        totalCount += saveRegions(reeList, RegionLevel.REE, regionMap);

        log.info("🎉 행정구역 데이터 총 {}건 저장 완료!", totalCount);
    }

    private List<VWorldRegionItem> fetchRegions(String endpoint, List<VWorldRegionItem> parentList) {
        if (parentList == null) {
            return vWorldClient.fetchRegions(endpoint, null);
        }

        List<VWorldRegionItem> result = new ArrayList<>();
        parentList.forEach(parent -> {
            List<VWorldRegionItem> temp = vWorldClient.fetchRegions(endpoint, parent.getAdmCode());
            result.addAll(temp);
        });

        return result;
    }

    private int saveRegions(List<VWorldRegionItem> regionInfos, RegionLevel level, Map<String, Region> regionMap) {
        List<Region> entities = regionInfos.stream()
                .map(info -> {
                    Region parent = info.getAdmCode() != null ? regionMap.get(info.getAdmCode()) : null;
                    return toEntity(info, level, parent);
                })
                .toList();

        List<Region> savedRegions = regionRepository.saveAll(entities);
        savedRegions.forEach(region -> regionMap.put(region.getCode(), region));

        log.info("{} {}건 저장 완료", level.getDescription(), savedRegions.size());
        return savedRegions.size();
    }

    private Region toEntity(VWorldRegionItem item, RegionLevel level, Region parent) {
        return Region.builder()
                .code(item.getAdmCode())
                .name(item.getAdmCodeNm() != null ? item.getAdmCodeNm() : item.getLowestAdmCodeNm())
                .level(level)
                .parent(parent)
                .build();
    }
}