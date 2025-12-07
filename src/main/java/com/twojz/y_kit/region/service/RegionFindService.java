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

    public Region findRegionFullName(String fullName) {
        return regionRepository.findByFullName(fullName)
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

    // RegionFindService.java에 추가
    public Region findRegionByAddress(String sido, String sigungu, String dong) {
        // 1순위: 동 (sido, sigungu와 함께 검증)
        if (dong != null && !dong.isEmpty()) {
            try {
                // 동 이름으로 먼저 찾기
                List<Region> dongRegions = regionRepository.findAllByNameAndLevel(dong, RegionLevel.DONG);

                // 여러 개면 상위 지역으로 필터링
                if (!dongRegions.isEmpty()) {
                    for (Region dongRegion : dongRegions) {
                        if (matchesParentHierarchy(dongRegion, sido, sigungu)) {
                            return dongRegion;
                        }
                    }
                }
            } catch (Exception e) {
                // 동 찾기 실패하면 다음 단계로
            }
        }

        // 2순위: 시군구
        if (sigungu != null && !sigungu.isEmpty()) {
            try {
                List<Region> sigunguRegions = regionRepository.findAllByNameAndLevel(sigungu, RegionLevel.SIGUNGU);
                if (!sigunguRegions.isEmpty()) {
                    for (Region sigunguRegion : sigunguRegions) {
                        if (matchesParentName(sigunguRegion, sido)) {
                            return sigunguRegion;
                        }
                    }
                }
            } catch (Exception e) {
                // 시군구 찾기 실패하면 다음 단계로
            }
        }

        // 3순위: 시도
        if (sido != null && !sido.isEmpty()) {
            return regionRepository.findByNameAndLevel(sido, RegionLevel.SIDO)
                    .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));
        }

        throw new IllegalArgumentException("유효한 지역 정보가 없습니다.");
    }

    private boolean matchesParentHierarchy(Region dongRegion, String sido, String sigungu) {
        if (dongRegion.getParent() == null) return false;

        Region sigunguRegion = dongRegion.getParent();
        if (sigungu != null && !sigunguRegion.getName().contains(sigungu)) {
            return false;
        }

        if (sigunguRegion.getParent() == null) return false;
        Region sidoRegion = sigunguRegion.getParent();
        return sido == null || sidoRegion.getName().contains(sido);
    }

    private boolean matchesParentName(Region region, String parentName) {
        if (parentName == null || region.getParent() == null) return true;
        return region.getParent().getName().contains(parentName);
    }

    private List<RegionResponse> findChildrenByParentAndLevel(String parentCode, RegionLevel level) {
        return regionRepository.findByParent_CodeAndLevel(parentCode, level)
                .stream()
                .map(RegionResponse::from)
                .toList();
    }
}
