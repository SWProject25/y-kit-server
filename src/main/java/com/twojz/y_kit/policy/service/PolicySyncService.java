package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.entity.*;
import com.twojz.y_kit.policy.entity.enums.*;
import com.twojz.y_kit.policy.repository.*;
import com.twojz.y_kit.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySyncService {

    private final YouthPolicyClient youthPolicyClient;
    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final PolicyKeywordRepository policyKeywordRepository;
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

        PolicyDetailEntity detail = buildPolicyDetail(apiPolicy, policy);
        PolicyQualificationEntity qualification = buildQualification(apiPolicy, policy);
        PolicyApplicationEntity application = buildApplication(apiPolicy, policy);

        policy.setDetail(detail);
        policy.setQualification(qualification);
        policy.setApplication(application);

        mapRegions(apiPolicy, policy);
        mapDocuments(apiPolicy, policy);
        mapCategories(apiPolicy, policy);
        mapKeywords(apiPolicy, policy);

        policyRepository.save(policy);
        log.debug("정책 저장 완료: {}", apiPolicy.getPlcyNo());
    }

    private PolicyEntity findOrCreatePolicy(YouthPolicy apiPolicy) {
        return policyDetailRepository.findByPlcyNo(apiPolicy.getPlcyNo())
                .map(PolicyDetailEntity::getPolicy)
                .orElseGet(() -> PolicyEntity.builder()
                        .policyNo(apiPolicy.getPlcyNo())
                        .viewCount(parseIntOrDefault(apiPolicy.getInqCnt(), 0))
                        .isActive(true)
                        .build());
    }

    private PolicyDetailEntity buildPolicyDetail(YouthPolicy apiPolicy, PolicyEntity policy) {
        return PolicyDetailEntity.builder()
                .policy(policy)
                .plcyNo(apiPolicy.getPlcyNo())
                .plcyNm(apiPolicy.getPlcyNm())
                .plcyExplnCn(apiPolicy.getPlcyExplnCn())
                .plcySprtCn(apiPolicy.getPlcySprtCn())
                .sprvsnInstCdNm(apiPolicy.getSprvsnInstCdNm())
                .operInstCdNm(apiPolicy.getOperInstCdNm())
                .sprtSclCnt(apiPolicy.getSprtSclCnt())
                .bizPrdSeCd(apiPolicy.getBizPrdSeCd())
                .bizPrdBgngYmd(parseDate(apiPolicy.getBizPrdBgngYmd()))
                .bizPrdEndYmd(parseDate(apiPolicy.getBizPrdEndYmd()))
                .bizPrdEtcCn(apiPolicy.getBizPrdEtcCn())
                .etcMttrCn(apiPolicy.getEtcMttrCn())
                .refUrlAddr1(apiPolicy.getRefUrlAddr1())
                .refUrlAddr2(apiPolicy.getRefUrlAddr2())
                .prtcpSgstTrgtCn(apiPolicy.getPtcpPrpTrgtCn())
                .build();
    }

    private PolicyQualificationEntity buildQualification(YouthPolicy apiPolicy, PolicyEntity policy) {
        return PolicyQualificationEntity.builder()
                .policy(policy)
                .sprtTrgtAgeLmttYn(apiPolicy.getSprtTrgtAgeLmtYn())
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
                .addAplyQlfcnCn(apiPolicy.getAddAplyQlfcCndCn())
                .build();
    }

    private PolicyApplicationEntity buildApplication(YouthPolicy apiPolicy, PolicyEntity policy) {
        return PolicyApplicationEntity.builder()
                .policy(policy)
                .sprtSclLmttYn(apiPolicy.getSprtSclLmtYn())
                .sprtArvlSqncYn(apiPolicy.getSprtArvlSeqYn())
                .aplyPrdSeCd(ApplicationPeriodType.fromCode(apiPolicy.getAplyPrdSeCd()))
                .aplyYmd(apiPolicy.getAplyYmd())
                .plcyAplyMthdCn(apiPolicy.getPlcyAplyMthdCn())
                .aplyUrlAddr(apiPolicy.getAplyUrlAddr())
                .scrnMthdCn(apiPolicy.getSrngMthdCn())
                .build();
    }

    private void mapRegions(YouthPolicy apiPolicy, PolicyEntity policy) {
        policy.getPolicyRegions().clear();
        if (apiPolicy.getZipCd() == null || apiPolicy.getZipCd().isBlank()) return;

        for (String zip : apiPolicy.getZipCd().split(",")) {
            String trimmed = zip.trim();
            if (!trimmed.isEmpty()) {
                regionRepository.findById(trimmed)
                        .ifPresent(region -> policy.getPolicyRegions().add(
                                PolicyRegion.builder().policy(policy).region(region).build()
                        ));
            }
        }
    }

    private void mapDocuments(YouthPolicy apiPolicy, PolicyEntity policy) {
        policy.getDocuments().clear();
        if (apiPolicy.getSbmsnDcmntCn() == null || apiPolicy.getSbmsnDcmntCn().isBlank()) return;

        for (String doc : apiPolicy.getSbmsnDcmntCn().split("\n")) {
            String trimmed = doc.trim();
            if (!trimmed.isEmpty()) {
                policy.getDocuments().add(
                        PolicyDocumentEntity.builder()
                                .policy(policy)
                                .documentsOriginal(trimmed)
                                .isRequired(true)
                                .build()
                );
            }
        }
    }

    private void mapCategories(YouthPolicy apiPolicy, PolicyEntity policy) {
        policy.getCategoryMappings().clear();

        if (apiPolicy.getLclsfNm() == null || apiPolicy.getLclsfNm().isBlank()) return;

        PolicyCategoryEntity main = findOrCreateCategory(apiPolicy.getLclsfNm().trim(), 1, null);
        policy.getCategoryMappings().add(
                PolicyCategoryMapping.builder().policy(policy).category(main).build()
        );

        if (apiPolicy.getMclsfNm() != null && !apiPolicy.getMclsfNm().isBlank()) {
            PolicyCategoryEntity sub = findOrCreateCategory(apiPolicy.getMclsfNm().trim(), 2, main);
            policy.getCategoryMappings().add(
                    PolicyCategoryMapping.builder().policy(policy).category(sub).build()
            );
        }
    }

    private void mapKeywords(YouthPolicy apiPolicy, PolicyEntity policy) {
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

    private Integer parseIntOrDefault(String value, int defaultValue) {
        Integer parsed = parseIntOrNull(value);
        return parsed != null ? parsed : defaultValue;
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