package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.entity.enums.ApplicationPeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_application", indexes = {
        @Index(name = "idx_policy_id", columnList = "policy_id")
})
@Entity
public class PolicyApplicationEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    private String sprtSclLmttYn;

    private String sprtArvlSqncYn;

    @Enumerated(EnumType.STRING)
    private ApplicationPeriodType aplyPrdSeCd;

    private String aplyYmd;

    @Column(columnDefinition = "TEXT")
    private String plcyAplyMthdCn;

    @Column(length = 1000)
    private String aplyUrlAddr;

    @Column(columnDefinition = "TEXT")
    private String scrnMthdCn;
}
