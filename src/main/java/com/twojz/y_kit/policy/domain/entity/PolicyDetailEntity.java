package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.enumType.BusinessPeriodType;
import com.twojz.y_kit.policy.domain.enumType.PolicyApprovalStatus;
import com.twojz.y_kit.policy.domain.enumType.PolicyProvisionMethod;
import com.twojz.y_kit.policy.dto.request.PolicyDetailUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_details")
@Entity
public class PolicyDetailEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    private String plcyNm;                              // 정책명

    @Column(columnDefinition = "TEXT")
    private String plcyExplnCn;                         // 정책설명

    private String sprvsnInstCdNm;                      // 주관기관명

    private String operInstCdNm;                        // 운영기관명

    @Enumerated(EnumType.STRING)
    @Column(name = "plcy_aprv_stts_cd", length = 20)
    private PolicyApprovalStatus plcyAprvSttsCd;        // 정책승인구분코드

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PolicyProvisionMethod plcyPvsnMthdCd;       // 정책제공방법코드

    @Column(columnDefinition = "TEXT")
    private String plcySprtCn;                          // 정책 지원 설명

    private String sprtSclCnt;                          // 지원규모 수

    @Column(columnDefinition = "TEXT")
    private String ptcpPrpTrgtCn;                       // 참여제안 대상 내용

    @Column(columnDefinition = "TEXT")
    private String srngMthdCn;                          // 심사방법 내용

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BusinessPeriodType bizPrdSeCd;              // 사업기간구분코드

    private LocalDate bizPrdBgngYmd;                    // 사업기간시작일자

    private LocalDate bizPrdEndYmd;                     // 사업기간종료일자

    @Column(columnDefinition = "TEXT")
    private String bizPrdEtcCn;                         // 사업기간 기타내용

    @Column(columnDefinition = "TEXT")
    private String etcMttrCn;                           // 기타사항 내용

    @Column(length = 1000)
    private String refUrlAddr1;                         // 참고 URL1

    @Column(length = 1000)
    private String refUrlAddr2;                         // 참고 URL2

    public void updateFromApi(PolicyDetailUpdateRequest dto) {
        this.plcyNm = dto.getPlcyNm();
        this.plcyExplnCn = dto.getPlcyExplnCn();
        this.plcyAprvSttsCd = dto.getPlcyAprvSttsCd();
        this.plcyPvsnMthdCd = dto.getPlcyPvsnMthdCd();
        this.sprvsnInstCdNm = dto.getSprvsnInstCdNm();
        this.operInstCdNm = dto.getOperInstCdNm();
        this.plcySprtCn = dto.getPlcySprtCn();
        this.sprtSclCnt = dto.getSprtSclCnt();
        this.srngMthdCn = dto.getSrngMthdCn();
        this.bizPrdSeCd = dto.getBizPrdSeCd();
        this.bizPrdBgngYmd = dto.getBizPrdBgngYmd();
        this.bizPrdEndYmd = dto.getBizPrdEndYmd();
        this.bizPrdEtcCn = dto.getBizPrdEtcCn();
        this.etcMttrCn = dto.getEtcMttrCn();
        this.refUrlAddr1 = dto.getRefUrlAddr1();
        this.refUrlAddr2 = dto.getRefUrlAddr2();
        this.ptcpPrpTrgtCn = dto.getPtcpPrpTrgtCn();
    }
}
