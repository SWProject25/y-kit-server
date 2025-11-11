package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.enumType.*;
import com.twojz.y_kit.policy.dto.request.*;
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

    public PolicyDetailUpdateRequest toDetailRequest(YouthPolicy src) {
        return PolicyDetailUpdateRequest.builder()
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

    public PolicyApplicationUpdateRequest toApplicationRequest(YouthPolicy src) {
        LocalDate startDate = null, endDate = null;
        if (StringUtils.hasText(src.getAplyYmd())) {
            String[] parts = src.getAplyYmd().split("~");
            if (parts.length > 0) startDate = parseDate(parts[0]);
            if (parts.length > 1) endDate = parseDate(parts[1]);
        }

        return PolicyApplicationUpdateRequest.builder()
                .sprtSclLmtYn(src.getSprtSclLmtYn())
                .sprtArvlSqncYn(src.getSprtArvlSeqYn())
                .aplyPrdSeCd(ApplicationPeriodType.fromCode(src.getAplyPrdSeCd()))
                .aplyBgngYmd(startDate)
                .aplyEndYmd(endDate)
                .plcyAplyMthdCn(src.getPlcyAplyMthdCn())
                .aplyUrlAddr(src.getAplyUrlAddr())
                .build();
    }

    public PolicyQualificationUpdateRequest toQualificationRequest(YouthPolicy src) {
        return PolicyQualificationUpdateRequest.builder()
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

    public PolicyDocumentUpdateRequest toDocumentRequest(YouthPolicy src) {
        return PolicyDocumentUpdateRequest.builder()
                .documentsOriginal(src.getSbmsnDcmntCn())
                .isRequired(true)
                .build();
    }

    // ===== Util =====
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