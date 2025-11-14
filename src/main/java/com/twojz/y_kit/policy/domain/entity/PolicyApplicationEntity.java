package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.enumType.ApplicationPeriodType;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_application")
@Entity
public class PolicyApplicationEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    private String sprtSclLmtYn;                    // 지원규모제한여부

    private String sprtArvlSqncYn;                  // 지원도착순서여부

    @Enumerated(EnumType.STRING)
    private ApplicationPeriodType aplyPrdSeCd;      // 신청기간구분코드

    private LocalDate aplyBgngYmd;                     // 신청시작일자
    private LocalDate aplyEndYmd;                      // 신청종료일자

    @Column(columnDefinition = "TEXT")
    private String plcyAplyMthdCn;                  // 정책신청방법내용

    @Column(length = 1000)
    private String aplyUrlAddr;                     // 신청 URL

    public void updateFromApi(PolicyApplicationDto dto) {
        this.sprtSclLmtYn = dto.getSprtSclLmtYn();
        this.sprtArvlSqncYn = dto.getSprtArvlSqncYn();
        this.aplyPrdSeCd = dto.getAplyPrdSeCd();
        this.aplyBgngYmd = dto.getAplyBgngYmd();
        this.aplyEndYmd = dto.getAplyEndYmd();
        this.plcyAplyMthdCn = dto.getPlcyAplyMthdCn();
        this.aplyUrlAddr = dto.getAplyUrlAddr();
    }
}
