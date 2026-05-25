package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.service.persistence.PolicyCategoryPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyKeywordPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyRegionPersistenceService;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.repository.RegionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PolicyMappingSyncService {

    private static final String CATEGORY_SEPARATOR_REGEX = "\\s*(,|및)\\s*";

    private final PolicyCategoryPersistenceService policyCategoryPersistenceService;
    private final PolicyKeywordPersistenceService policyKeywordPersistenceService;
    private final PolicyRegionPersistenceService policyRegionPersistenceService;
    private final RegionRepository regionRepository;

    public MappingSyncContext createContext() {
        return new MappingSyncContext(new HashMap<>(), new HashMap<>());
    }

    public boolean syncMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyCategoryMapping> existingCategoryMappings,
            List<PolicyKeywordMapping> existingKeywordMappings,
            List<PolicyRegion> existingRegionMappings,
            MappingSyncContext context
    ) {
        boolean categoryChanged = updateCategoryMappings(
                apiPolicy, policy, existingCategoryMappings, context.categoryCache());
        boolean keywordChanged = updateKeywordMappings(
                apiPolicy, policy, existingKeywordMappings, context.keywordCache());
        boolean regionChanged = updateRegionMappings(apiPolicy, policy, existingRegionMappings);

        return categoryChanged || keywordChanged || regionChanged;
    }

    private boolean updateCategoryMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyCategoryMapping> existingMappings,
            Map<String, PolicyCategoryEntity> categoryCache
    ) {
        Function<String, Set<String>> parse = txt -> Arrays.stream(txt.split(CATEGORY_SEPARATOR_REGEX))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PolicyCategoryMapping> newMappings = new ArrayList<>();

        if (StringUtils.hasText(apiPolicy.getLclsfNm())) {
            parse.apply(apiPolicy.getLclsfNm()).forEach(name -> {
                PolicyCategoryEntity main = findOrCreateCategoryWithCache(name, 1, null, categoryCache);
                newMappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(main)
                        .build());
            });
        }

        if (StringUtils.hasText(apiPolicy.getMclsfNm())) {
            PolicyCategoryEntity mainParent = newMappings.isEmpty() ? null : newMappings.getFirst().getCategory();

            parse.apply(apiPolicy.getMclsfNm()).forEach(name -> {
                PolicyCategoryEntity sub = findOrCreateCategoryWithCache(name, 2, mainParent, categoryCache);
                newMappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(sub)
                        .build());
            });
        }

        List<Long> existingCategoryIds = existingMappings.stream()
                .map(m -> m.getCategory().getId())
                .toList();
        List<Long> newCategoryIds = newMappings.stream()
                .map(m -> m.getCategory().getId())
                .toList();

        if (existingCategoryIds.equals(newCategoryIds)) {
            return false;
        }

        policyCategoryPersistenceService.deleteMappings(existingMappings);
        policyCategoryPersistenceService.saveMappings(newMappings);

        return true;
    }

    private boolean updateKeywordMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyKeywordMapping> existingMappings,
            Map<String, PolicyKeywordEntity> keywordCache
    ) {
        Map<String, PolicyKeywordMapping> existingKeywordMap = existingMappings.stream()
                .collect(Collectors.toMap(
                        m -> m.getKeyword().getKeyword(),
                        m -> m
                ));

        final Set<String> newKeywords;
        if (StringUtils.hasText(apiPolicy.getPlcyKywdNm())) {
            newKeywords = Arrays.stream(apiPolicy.getPlcyKywdNm().split(","))
                    .map(String::trim)
                    .filter(kw -> !kw.isEmpty())
                    .collect(Collectors.toSet());
        } else {
            newKeywords = Collections.emptySet();
        }

        List<PolicyKeywordMapping> toDelete = existingMappings.stream()
                .filter(m -> !newKeywords.contains(m.getKeyword().getKeyword()))
                .toList();

        Set<String> toAddKeywords = new HashSet<>(newKeywords);
        toAddKeywords.removeAll(existingKeywordMap.keySet());

        if (toDelete.isEmpty() && toAddKeywords.isEmpty()) {
            return false;
        }

        if (!toDelete.isEmpty()) {
            toDelete.forEach(m -> m.getKeyword().decreaseUsageCount());
            policyKeywordPersistenceService.deleteMappings(toDelete);
        }

        if (!toAddKeywords.isEmpty()) {
            List<PolicyKeywordMapping> toAdd = toAddKeywords.stream()
                    .map(kw -> {
                        PolicyKeywordEntity keyword = findOrCreateKeywordWithCache(kw, keywordCache);
                        keyword.increaseUsageCount();
                        return PolicyKeywordMapping.builder()
                                .policy(policy)
                                .keyword(keyword)
                                .build();
                    })
                    .toList();
            policyKeywordPersistenceService.saveMappings(toAdd);
        }

        return true;
    }

    private PolicyCategoryEntity findOrCreateCategoryWithCache(
            String name,
            int level,
            PolicyCategoryEntity parent,
            Map<String, PolicyCategoryEntity> cache
    ) {
        String cacheKey = name + "_" + level;
        return cache.computeIfAbsent(cacheKey, key ->
                policyCategoryPersistenceService.findByNameAndLevel(name, level)
                        .orElseGet(() -> policyCategoryPersistenceService.save(
                                PolicyCategoryEntity.builder()
                                        .name(name)
                                        .level(level)
                                        .parent(parent)
                                        .isActive(true)
                                        .build()
                        ))
        );
    }

    private PolicyKeywordEntity findOrCreateKeywordWithCache(
            String keywordText,
            Map<String, PolicyKeywordEntity> cache
    ) {
        return cache.computeIfAbsent(keywordText, key ->
                policyKeywordPersistenceService.findByKeyword(keywordText)
                        .orElseGet(() -> policyKeywordPersistenceService.save(
                                PolicyKeywordEntity.builder()
                                        .keyword(keywordText)
                                        .usageCount(0)
                                        .build()
                        ))
        );
    }

    private boolean updateRegionMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyRegion> existingMappings
    ) {
        Set<String> existingRegionIds = existingMappings.stream()
                .map(pr -> pr.getRegion().getCode())
                .collect(Collectors.toSet());

        Set<String> newRegionCodes = new HashSet<>();
        if (StringUtils.hasText(apiPolicy.getZipCd())) {
            newRegionCodes = Arrays.stream(apiPolicy.getZipCd().split("[,;]"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }

        List<Region> newRegions = regionRepository.findAllByCodeIn(new ArrayList<>(newRegionCodes));
        Set<String> newRegionIds = newRegions.stream()
                .map(Region::getCode)
                .collect(Collectors.toSet());

        List<PolicyRegion> toDelete = existingMappings.stream()
                .filter(pr -> !newRegionIds.contains(pr.getRegion().getCode()))
                .toList();

        Set<String> toAddIds = new HashSet<>(newRegionIds);
        toAddIds.removeAll(existingRegionIds);

        if (toDelete.isEmpty() && toAddIds.isEmpty()) {
            return false;
        }

        if (!toDelete.isEmpty()) {
            policyRegionPersistenceService.deleteAll(toDelete);
        }

        if (!toAddIds.isEmpty()) {
            List<PolicyRegion> toAdd = newRegions.stream()
                    .filter(region -> toAddIds.contains(region.getCode()))
                    .map(region -> PolicyRegion.builder()
                            .policy(policy)
                            .region(region)
                            .build())
                    .toList();
            policyRegionPersistenceService.saveAll(toAdd);
        }

        return true;
    }

    public record MappingSyncContext(
            Map<String, PolicyCategoryEntity> categoryCache,
            Map<String, PolicyKeywordEntity> keywordCache
    ) {}
}
