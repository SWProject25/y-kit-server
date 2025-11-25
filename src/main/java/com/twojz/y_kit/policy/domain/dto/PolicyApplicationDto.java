package com.twojz.y_kit.policy.domain.dto;

import com.twojz.y_kit.policy.domain.enumType.ApplicationPeriodType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolicyApplicationDto {
    private String sprtSclLmtYn;
    private String sprtArvlSqncYn;
    private ApplicationPeriodType aplyPrdSeCd;
    private LocalDate aplyBgngYmd;
    private LocalDate aplyEndYmd;
    private String plcyAplyMthdCn;
    private String aplyUrlAddr;
}
