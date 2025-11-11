package com.twojz.y_kit.policy.dto.request;

import com.twojz.y_kit.policy.domain.enumType.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PolicyDetailUpdateRequest {
    private String plcyNm;
    private String plcyExplnCn;
    private String sprvsnInstCdNm;
    private String operInstCdNm;
    private PolicyApprovalStatus plcyAprvSttsCd;
    private PolicyProvisionMethod plcyPvsnMthdCd;
    private String plcySprtCn;
    private String sprtSclCnt;
    private String ptcpPrpTrgtCn;
    private String srngMthdCn;
    private BusinessPeriodType bizPrdSeCd;
    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd;
    private String bizPrdEtcCn;
    private String etcMttrCn;
    private String refUrlAddr1;
    private String refUrlAddr2;
}