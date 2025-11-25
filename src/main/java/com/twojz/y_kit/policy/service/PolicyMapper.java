package com.twojz.y_kit.policy.service;

import static com.twojz.y_kit.policy.dto.response.PolicyDetailResponse.toKeywords;

import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import com.twojz.y_kit.policy.domain.dto.PolicyDetailDto;
import com.twojz.y_kit.policy.domain.dto.PolicyQualificationDto;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.enumType.*;
import com.twojz.y_kit.policy.dto.response.AiAnalysisInfo;
import com.twojz.y_kit.policy.dto.response.CategoryInfo;
import com.twojz.y_kit.policy.dto.response.PolicyApplication;
import com.twojz.y_kit.policy.dto.response.PolicyBasicInfo;
import com.twojz.y_kit.policy.dto.response.PolicyDetail;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDocument;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.dto.response.PolicyQualification;
import com.twojz.y_kit.policy.dto.response.RegionInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class PolicyMapper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PolicyDetailDto toDetailRequest(YouthPolicy src) {
        return PolicyDetailDto.builder()
                .plcyNm(src.getPlcyNm())
                .plcyExplnCn(src.getPlcyExplnCn())
                .sprvsnInstCdNm(src.getSprvsnInstCdNm())
                .operInstCdNm(src.getOperInstCdNm())
                .plcyAprvSttsCd(PolicyApprovalStatus.fromCode(src.getPlcyAprvSttsCd()))
                .plcyPvsnMthdCd(PolicyProvisionMethod.fromCode(src.getPlcyPvsnMthdCd()))
                .plcySprtCn(src.getPlcySprtCn())
                .sprtSclCnt(src.getSprtSclCnt())
                .ptcpPrpTrgtCn(src.getPtcpPrpTrgtCn())
                .srngMthdCn(src.getSrngMthdCn())
                .bizPrdSeCd(BusinessPeriodType.fromCode(src.getBizPrdSeCd()))
                .bizPrdBgngYmd(parseDate(src.getBizPrdBgngYmd()))
                .bizPrdEndYmd(parseDate(src.getBizPrdEndYmd()))
                .bizPrdEtcCn(src.getBizPrdEtcCn())
                .etcMttrCn(src.getEtcMttrCn())
                .refUrlAddr1(src.getRefUrlAddr1())
                .refUrlAddr2(src.getRefUrlAddr2())
                .build();
    }

    public PolicyApplicationDto toApplicationRequest(YouthPolicy src) {
        LocalDate startDate = null, endDate = null;
        if (StringUtils.hasText(src.getAplyYmd())) {
            String[] parts = src.getAplyYmd().split("~");
            if (parts.length > 0) startDate = parseDate(parts[0]);
            if (parts.length > 1) endDate = parseDate(parts[1]);
        }

        return PolicyApplicationDto.builder()
                .sprtSclLmtYn(src.getSprtSclLmtYn())
                .sprtArvlSqncYn(src.getSprtArvlSeqYn())
                .aplyPrdSeCd(ApplicationPeriodType.fromCode(src.getAplyPrdSeCd()))
                .aplyBgngYmd(startDate)
                .aplyEndYmd(endDate)
                .plcyAplyMthdCn(src.getPlcyAplyMthdCn())
                .aplyUrlAddr(src.getAplyUrlAddr())
                .build();
    }

    public PolicyQualificationDto toQualificationRequest(YouthPolicy src) {
        return PolicyQualificationDto.builder()
                .sprtTrgtAgeLmtYn(src.getSprtTrgtAgeLmtYn())
                .sprtTrgtMinAge(parseIntOrNull(src.getSprtTrgtMinAge()))
                .sprtTrgtMaxAge(parseIntOrNull(src.getSprtTrgtMaxAge()))
                .earnCndSeCd(IncomeConditionType.fromCode(src.getEarnCndSeCd()))
                .earnMinAmt(parseBigDecimalOrNull(src.getEarnMinAmt()))
                .earnMaxAmt(parseBigDecimalOrNull(src.getEarnMaxAmt()))
                .earnEtcCn(src.getEarnEtcCn())
                .mrgSttsCd(MaritalStatus.fromCode(src.getMrgSttsCd()))
                .schoolCd(EducationLevel.fromCode(src.getSchoolCd()))
                .jobCd(EmploymentStatus.fromCode(src.getJobCd()))
                .plcyMajorCd(MajorField.fromCode(src.getPlcyMajorCd()))
                .sBizCd(SpecializedRequirement.fromCode(src.getSBizCd()))
                .addAplyQlfcCndCn(src.getAddAplyQlfcCndCn())
                .build();
    }

    /**
     * Entity -> 응답 DTO
     */
    public PolicyListResponse toListResponse(PolicyEntity entity) {
        PolicyDetailEntity detail = entity.getDetail();
        PolicyApplicationEntity application = entity.getApplication();
        PolicyQualificationEntity qualification = entity.getQualification();

        // 카테고리 추출
        String largeCategory = null;
        String mediumCategory = null;
        if (entity.getCategoryMappings() != null) {
            for (PolicyCategoryMapping mapping : entity.getCategoryMappings()) {
                PolicyCategoryEntity category = mapping.getCategory();
                if (category.getLevel() == 1) {
                    largeCategory = category.getName();
                } else if (category.getLevel() == 2) {
                    mediumCategory = category.getName();
                }
            }
        }

        // 키워드 추출
        List<String> keywords = entity.getKeywordMappings() != null ?
                entity.getKeywordMappings().stream()
                        .map(mapping -> mapping.getKeyword().getKeyword())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        // 지역 추출
        List<String> regions = entity.getRegions() != null ?
                entity.getRegions().stream()
                        .map(policyRegion -> policyRegion.getRegion().getName())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return PolicyListResponse.builder()
                .policyId(entity.getId())
                .policyNo(entity.getPolicyNo())
                .policyName(detail != null ? detail.getPlcyNm() : null)
                .summary(detail != null ? truncate(detail.getPlcyExplnCn()) : null)
                .largeCategory(largeCategory)
                .mediumCategory(mediumCategory)
                .isApplicationAvailable(isApplicationAvailable(application))
                .applicationStartDate(application != null ? application.getAplyBgngYmd() : null)
                .applicationEndDate(application != null ? application.getAplyEndYmd() : null)
                .supervisingInstitution(detail != null ? detail.getSprvsnInstCdNm() : null)
                .minAge(qualification != null ? qualification.getSprtTrgtMinAge() : null)
                .maxAge(qualification != null ? qualification.getSprtTrgtMaxAge() : null)
                .keywords(keywords)
                .regions(regions)
                .viewCount(entity.getViewCount())
                .bookmarkCount(entity.getBookmarkCount())
                .applicationCount(entity.getApplicationCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PolicyDetailResponse toDetailResponse(PolicyEntity entity) {
        return PolicyDetailResponse.builder()
                .basicInfo(PolicyBasicInfo.from(entity))
                .detail(PolicyDetail.from(entity.getDetail()))
                .application(PolicyApplication.from(entity.getApplication()))
                .qualification(PolicyQualification.from(entity.getQualification()))
                .document(PolicyDocument.from(entity.getDocument()))
                .categories(CategoryInfo.from(entity.getCategoryMappings()))
                .keywords(toKeywords(entity.getKeywordMappings()))
                .regions(RegionInfo.from(entity.getRegions()))
                .aiAnalysis(AiAnalysisInfo.from(entity))
                .build();
    }


    /**
     * Util 메서드
     */
    private Boolean isApplicationAvailable(PolicyApplicationEntity application) {
        if (application == null || application.getAplyBgngYmd() == null || application.getAplyEndYmd() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return !today.isBefore(application.getAplyBgngYmd()) && !today.isAfter(application.getAplyEndYmd());
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() <= 100) {
            return text;
        }
        return text.substring(0, 100) + "...";
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }

    private Integer parseIntOrNull(String val) {
        if (!StringUtils.hasText(val)) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            log.warn("int 파싱 실패: {}", val);
            return null;
        }
    }

    private BigDecimal parseBigDecimalOrNull(String val) {
        if (!StringUtils.hasText(val)) return null;
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            log.warn("BigDecimal 파싱 실패: {}", val);
            return null;
        }
    }
}