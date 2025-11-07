package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.entity.*;
import com.twojz.y_kit.policy.domain.enumType.*;
import com.twojz.y_kit.policy.repository.*;
import com.twojz.y_kit.region.repository.RegionRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySyncService {
    private final YouthPolicyClient youthPolicyClient;
    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyApplicationRepository policyApplicationRepository;
    private final PolicyQualificationRepository policyQualificationRepository;
    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final PolicyCategoryMappingRepository policyCategoryMappingRepository;
    private final PolicyKeywordRepository policyKeywordRepository;
    private final PolicyKeywordMappingRepository policyKeywordMappingRepository;
    private final PolicyRegionRepository policyRegionRepository;
    private final RegionRepository regionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 정책 전체 동기화 */
    @Transactional
    public void syncAllPolicies() {
        log.info("정책 동기화 시작");

        youthPolicyClient.fetchAllPolicies()
                .subscribe(
                        policies -> {
                            log.info("총 {}개 정책 동기화 시작", policies.size());

                            int successCount = 0;
                            int failCount = 0;

                            for (YouthPolicy apiPolicy : policies) {
                                try {
                                    saveOrUpdatePolicy(apiPolicy);
                                    successCount++;

                                    if (successCount % 100 == 0) {
                                        log.info("진행 중: {}/{}", successCount, policies.size());
                                    }
                                } catch (Exception e) {
                                    log.error("정책 저장 실패: {}", apiPolicy.getPlcyNo(), e);
                                    failCount++;
                                }
                            }

                            log.info("정책 동기화 완료 - 성공: {}, 실패: {}", successCount, failCount);
                        },
                        error -> log.error("정책 동기화 중 오류 발생", error)
                );
    }

    /** 단일 정책 저장/업데이트 */
    @Transactional
    public void saveOrUpdatePolicy(YouthPolicy apiPolicy) {
        PolicyEntity policy = findOrCreatePolicy(apiPolicy);

        if (policy.getId() != null) {
            policyDetailRepository.deleteByPolicy(policy);
            policyApplicationRepository.deleteByPolicy(policy);
            policyQualificationRepository.deleteByPolicy(policy);

            policyDocumentRepository.deleteByPolicy(policy);
            policyCategoryMappingRepository.deleteByPolicy(policy);

            policyKeywordMappingRepository.findByPolicyWithKeyword(policy).forEach(km -> {
                km.getKeyword().decreaseUsageCount();
            });
            policyKeywordMappingRepository.deleteByPolicy(policy);

            policyRegionRepository.deleteByPolicy(policy);
        }

        PolicyDetailEntity detail = buildPolicyDetail(apiPolicy, policy);
        policyDetailRepository.save(detail);

        PolicyApplicationEntity application = buildApplication(apiPolicy, policy);
        policyApplicationRepository.save(application);

        PolicyQualificationEntity qualification = buildQualification(apiPolicy, policy);
        policyQualificationRepository.save(qualification);

        buildDocuments(apiPolicy, policy);

        mappingCategories(apiPolicy, policy);

        mappingKeywords(apiPolicy, policy);

        mappingRegions(apiPolicy, policy);

        policyRepository.save(policy);
        log.debug("정책 저장 완료: {}", apiPolicy.getPlcyNo());
    }

    private PolicyEntity findOrCreatePolicy(YouthPolicy apiPolicy) {
        return policyRepository.findByPolicyNo(apiPolicy.getPlcyNo())
                .orElseGet(() -> {
                    PolicyEntity newPolicy = PolicyEntity.builder()
                            .policyNo(apiPolicy.getPlcyNo())
                            .isActive(true)
                            .build();
                    return policyRepository.save(newPolicy);
                });
    }

    private PolicyDetailEntity buildPolicyDetail(YouthPolicy apiPolicy, PolicyEntity policy) {
        return PolicyDetailEntity.builder()
                .policy(policy)
                .plcyNm(apiPolicy.getPlcyNm())
                .plcyExplnCn(apiPolicy.getPlcyExplnCn())
                .plcyAprvSttsCd(PolicyApprovalStatus.fromCode(apiPolicy.getPlcyAprvSttsCd()))
                .plcyPvsnMthdCd(PolicyProvisionMethod.fromCode(apiPolicy.getPlcyPvsnMthdCd()))
                .sprvsnInstCdNm(apiPolicy.getSprvsnInstCdNm())
                .operInstCdNm(apiPolicy.getOperInstCdNm())
                .plcySprtCn(apiPolicy.getPlcySprtCn())
                .sprtSclCnt(apiPolicy.getSprtSclCnt())
                .srngMthdCn(apiPolicy.getSrngMthdCn())
                .bizPrdSeCd(BusinessPeriodType.fromCode(apiPolicy.getBizPrdSeCd()))
                .bizPrdBgngYmd(parseDate(apiPolicy.getBizPrdBgngYmd()))
                .bizPrdEndYmd(parseDate(apiPolicy.getBizPrdEndYmd()))
                .bizPrdEtcCn(apiPolicy.getBizPrdEtcCn())
                .etcMttrCn(apiPolicy.getEtcMttrCn())
                .refUrlAddr1(apiPolicy.getRefUrlAddr1())
                .refUrlAddr2(apiPolicy.getRefUrlAddr2())
                .ptcpPrpTrgtCn(apiPolicy.getPtcpPrpTrgtCn())
                .build();
    }

    private PolicyApplicationEntity buildApplication(YouthPolicy apiPolicy, PolicyEntity policy) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (StringUtils.hasText(apiPolicy.getAplyYmd())) {
            String[] aplyYmd = apiPolicy.getAplyYmd().split("~");
            if (aplyYmd.length >= 1) {
                startDate = parseDate(aplyYmd[0]);
            }
            if (aplyYmd.length >= 2) {
                endDate = parseDate(aplyYmd[1]);
            }
        }

        return PolicyApplicationEntity.builder()
                .policy(policy)
                .sprtSclLmtYn(apiPolicy.getSprtSclLmtYn())
                .sprtArvlSqncYn(apiPolicy.getSprtArvlSeqYn())
                .aplyPrdSeCd(ApplicationPeriodType.fromCode(apiPolicy.getAplyPrdSeCd()))
                .aplyBgngYmd(startDate)
                .aplyEndYmd(endDate)
                .plcyAplyMthdCn(apiPolicy.getPlcyAplyMthdCn())
                .aplyUrlAddr(apiPolicy.getAplyUrlAddr())
                .build();
    }

    private PolicyQualificationEntity buildQualification(YouthPolicy apiPolicy, PolicyEntity policy) {
        return PolicyQualificationEntity.builder()
                .policy(policy)
                .sprtTrgtAgeLmtYn(apiPolicy.getSprtTrgtAgeLmtYn())
                .sprtTrgtMinAge(parseIntOrNull(apiPolicy.getSprtTrgtMinAge()))
                .sprtTrgtMaxAge(parseIntOrNull(apiPolicy.getSprtTrgtMaxAge()))
                .earnCndSeCd(IncomeConditionType.fromCode(apiPolicy.getEarnCndSeCd()))
                .earnMinAmt(parseBigDecimalOrNull(apiPolicy.getEarnMinAmt()))
                .earnMaxAmt(parseBigDecimalOrNull(apiPolicy.getEarnMaxAmt()))
                .earnEtcCn(apiPolicy.getEarnEtcCn())
                .mrgSttsCd(MaritalStatus.fromCode(apiPolicy.getMrgSttsCd()))
                .schoolCd(EducationLevel.fromCode(apiPolicy.getSchoolCd()))
                .jobCd(EmploymentStatus.fromCode(apiPolicy.getJobCd()))
                .plcyMajorCd(MajorField.fromCode(apiPolicy.getPlcyMajorCd()))
                .sbizCd(SpecializedRequirement.fromCode(apiPolicy.getSBizCd()))
                .addAplyQlfcCndCn(apiPolicy.getAddAplyQlfcCndCn())
                .build();
    }

    private void buildDocuments(YouthPolicy apiPolicy, PolicyEntity policy) {
        policyDocumentRepository.save(PolicyDocumentEntity.builder()
                .policy(policy)
                .documentsOriginal(apiPolicy.getSbmsnDcmntCn())
                .isRequired(true)
                .build());
    }

    private void mappingCategories(YouthPolicy apiPolicy, PolicyEntity policy) {
        policy.getCategoryMappings().clear();

        Function<String, Set<String>> parseCategories = (text) -> Arrays.stream(
                        text.split("\\s*(,|및)\\s*")
                )
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (apiPolicy.getLclsfNm() != null && !apiPolicy.getLclsfNm().isBlank()) {
            Set<String> mainCategories = parseCategories.apply(apiPolicy.getLclsfNm());
            for (String mainName : mainCategories) {
                PolicyCategoryEntity main = findOrCreateCategory(mainName, 1, null);
                policy.getCategoryMappings().add(
                        PolicyCategoryMapping.builder().policy(policy).category(main).build()
                );
            }
        }

        if (apiPolicy.getMclsfNm() != null && !apiPolicy.getMclsfNm().isBlank()) {
            Set<String> subCategories = parseCategories.apply(apiPolicy.getMclsfNm());
            PolicyCategoryEntity mainParent = null;
            if (!policy.getCategoryMappings().isEmpty()) {
                mainParent = policy.getCategoryMappings().iterator().next().getCategory();
            }
            for (String subName : subCategories) {
                PolicyCategoryEntity sub = findOrCreateCategory(subName, 2, mainParent);
                policy.getCategoryMappings().add(
                        PolicyCategoryMapping.builder().policy(policy).category(sub).build()
                );
            }
        }
    }

    private void mappingKeywords(YouthPolicy apiPolicy, PolicyEntity policy) {
        policy.getKeywordMappings().clear();
        if (apiPolicy.getPlcyKywdNm() == null || apiPolicy.getPlcyKywdNm().isBlank()) return;

        for (String kw : apiPolicy.getPlcyKywdNm().split(",")) {
            String trimmed = kw.trim();
            if (!trimmed.isEmpty()) {
                PolicyKeywordEntity keyword = findOrCreateKeyword(trimmed);
                policy.getKeywordMappings().add(
                        PolicyKeywordMapping.builder().policy(policy).keyword(keyword).build()
                );
            }
        }
    }

    private void mappingRegions(YouthPolicy apiPolicy, PolicyEntity policy) {
        if (!StringUtils.hasText(apiPolicy.getZipCd())) {
            return;
        }

        List<String> regionCodes = Arrays.stream(apiPolicy.getZipCd().split("[,;]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        for (String regionCode : regionCodes) {
            regionRepository.findByCode(regionCode).ifPresent(region -> {
                PolicyRegion policyRegion = PolicyRegion.builder()
                        .policy(policy)
                        .region(region)
                        .build();
                policyRegionRepository.save(policyRegion);
            });
        }
    }

    private PolicyCategoryEntity findOrCreateCategory(String name, int level, PolicyCategoryEntity parent) {
        return policyCategoryRepository.findByNameAndLevel(name, level)
                .orElseGet(() -> policyCategoryRepository.save(
                        PolicyCategoryEntity.builder()
                                .name(name)
                                .level(level)
                                .parent(parent)
                                .isActive(true)
                                .build()
                ));
    }

    private PolicyKeywordEntity findOrCreateKeyword(String keywordText) {
        return policyKeywordRepository.findByKeyword(keywordText)
                .orElseGet(() -> policyKeywordRepository.save(
                        PolicyKeywordEntity.builder()
                                .keyword(keywordText)
                                .usageCount(0)
                                .build()
                ));
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("숫자 파싱 실패: {}", value);
            return null;
        }
    }

    private BigDecimal parseBigDecimalOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("BigDecimal 파싱 실패: {}", value);
            return null;
        }
    }
}