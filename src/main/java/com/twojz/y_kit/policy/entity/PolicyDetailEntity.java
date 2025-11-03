package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_details", indexes = {
        @Index(name = "idx_policy_id", columnList = "policy_id"),
        @Index(name = "idx_plcy_no", columnList = "plcy_no")
})
@Entity
public class PolicyDetailEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    @Column(unique = true)
    private String plcyNo;

    private String plcyNm;

    @Column(columnDefinition = "TEXT")
    private String plcyExplnCn;

    @Column(columnDefinition = "TEXT")
    private String plcySprtCn;

    private String sprvsnInstCdNm;

    private String operInstCdNm;

    private String sprtSclCnt;

    private String bizPrdSeCd;

    private LocalDate bizPrdBgngYmd;

    private LocalDate bizPrdEndYmd;

    @Column(columnDefinition = "TEXT")
    private String bizPrdEtcCn;

    @Column(columnDefinition = "TEXT")
    private String etcMttrCn;

    @Column(length = 1000)
    private String refUrlAddr1;

    @Column(length = 1000)
    private String refUrlAddr2;

    @Column(columnDefinition = "TEXT")
    private String prtcpSgstTrgtCn;
}
