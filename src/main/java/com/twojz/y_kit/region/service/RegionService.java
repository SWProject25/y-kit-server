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

        int totalCount = 0;

        // 순차적으로 각 레벨 초기화
        totalCount += initSido();
        totalCount += initSigungu();
        totalCount += initDong();
        totalCount += initRee();

        log.info("🎉 행정구역 데이터 총 {}건 저장 완료!", totalCount);
    }

    @Transactional
    public int initSido() {
        return initRegionLevel(
                RegionLevel.SIDO,
                VWorldApiEndpoint.ADM_CODE_LIST_PATH,
                null
        );
    }

    @Transactional
    public int initSigungu() {
        return initRegionLevel(
                RegionLevel.SIGUNGU,
                VWorldApiEndpoint.ADM_SI_LIST_PATH,
                RegionLevel.SIDO
        );
    }

    @Transactional
    public int initDong() {
        return initRegionLevel(
                RegionLevel.DONG,
                VWorldApiEndpoint.ADM_DONG_LIST_PATH,
                RegionLevel.SIGUNGU
        );
    }

    @Transactional
    public int initRee() {
        return initRegionLevel(
                RegionLevel.REE,
                VWorldApiEndpoint.ADM_REE_LIST_PATH,
                RegionLevel.DONG
        );
    }

    private int initRegionLevel(RegionLevel level, String endpoint, RegionLevel parentLevel) {
        log.info("🚀 {} 데이터 초기화 시작", level.getDescription());

        List<VWorldRegionItem> items;

        if (parentLevel == null) {
            // 시도인 경우 - 부모 없이 바로 조회
            items = vWorldClient.fetchRegions(endpoint, null);
        } else {
            // 하위 레벨인 경우 - DB에서 부모 레벨 조회
            List<Region> parentRegions = regionRepository.findByLevel(parentLevel);
            if (parentRegions.isEmpty()) {
                log.warn("⚠️ {} 데이터가 없습니다. 먼저 {} 데이터를 초기화해주세요.",
                        parentLevel.getDescription(), parentLevel.getDescription());
                return 0;
            }

            items = fetchChildRegions(endpoint, parentRegions);
        }

        int count = saveRegionsWithParent(items, level, parentLevel);
        log.info("🎉 {} 데이터 {}건 저장 완료!", level.getDescription(), count);

        return count;
    }

    private List<VWorldRegionItem> fetchChildRegions(String endpoint, List<Region> parentRegions) {
        List<VWorldRegionItem> result = new ArrayList<>();
        int count = 0;
        int total = parentRegions.size();

        for (Region parent : parentRegions) {
            count++;
            try {
                List<VWorldRegionItem> temp = vWorldClient.fetchRegions(endpoint, parent.getCode());

                if (temp != null && !temp.isEmpty()) {
                    result.addAll(temp);
                    log.debug("✅ {} 하위 데이터 {}건 조회 (code: {})",
                            parent.getName(), temp.size(), parent.getCode());
                } else {
                    log.debug("⚠️ {} 하위 데이터 없음 (code: {})",
                            parent.getName(), parent.getCode());
                }

                if (count % 10 == 0) {
                    log.info("진행 중: {}/{} (누적 {}건)", count, total, result.size());
                }

                Thread.sleep(100);

            } catch (Exception e) {
                log.warn("❌ {} 조회 실패 ({}번째, code: {}): {}",
                        parent.getName(), count, parent.getCode(), e.getMessage());
            }
        }

        log.info("✅ 총 {}건 수집 완료 ({}/{}개 지역 조회)", result.size(), count, total);
        return result;
    }

    private int saveRegionsWithParent(List<VWorldRegionItem> regionInfos, RegionLevel level, RegionLevel parentLevel) {
        if (regionInfos.isEmpty()) {
            log.warn("⚠️ 저장할 데이터가 없습니다.");
            return 0;
        }

        // 부모 레벨이 있는 경우 DB에서 조회
        Map<String, Region> parentMap = new ConcurrentHashMap<>();
        if (parentLevel != null) {
            List<Region> parents = regionRepository.findByLevel(parentLevel);
            parents.forEach(parent -> parentMap.put(parent.getCode(), parent));
        }

        List<Region> entities = regionInfos.stream()
                .map(info -> {
                    String parentCode = getParentCode(info.getAdmCode(), level);
                    Region parent = parentCode != null ? parentMap.get(parentCode) : null;
                    return toEntity(info, level, parent);
                })
                .toList();

        List<Region> savedRegions = regionRepository.saveAll(entities);
        return savedRegions.size();
    }

    private int saveRegions(List<VWorldRegionItem> regionInfos, RegionLevel level, Map<String, Region> regionMap) {
        List<Region> entities = regionInfos.stream()
                .map(info -> {
                    String parentCode = getParentCode(info.getAdmCode(), level);
                    Region parent = parentCode != null ? regionMap.get(parentCode) : null;
                    return toEntity(info, level, parent);
                })
                .toList();

        List<Region> savedRegions = regionRepository.saveAll(entities);
        savedRegions.forEach(region -> regionMap.put(region.getCode(), region));

        return savedRegions.size();
    }

    private String getParentCode(String admCode, RegionLevel level) {
        if (admCode == null) return null;

        return switch (level) {
            case SIDO -> null;
            case SIGUNGU -> admCode.substring(0, 2);
            case DONG -> admCode.substring(0, 5);
            case REE -> admCode.substring(0, 8);
        };
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