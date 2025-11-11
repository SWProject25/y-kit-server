package com.twojz.y_kit.policy.dto.request;

import com.twojz.y_kit.policy.domain.enumType.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PolicyQualificationUpdateRequest {
    private String sprtTrgtAgeLmtYn;
    private Integer sprtTrgtMinAge;
    private Integer sprtTrgtMaxAge;
    private IncomeConditionType earnCndSeCd;
    private BigDecimal earnMinAmt;
    private BigDecimal earnMaxAmt;
    private String earnEtcCn;
    private MaritalStatus mrgSttsCd;
    private EducationLevel schoolCd;
    private EmploymentStatus jobCd;
    private MajorField plcyMajorCd;
    private SpecializedRequirement sBizCd;
    private String addAplyQlfcCndCn;
}
