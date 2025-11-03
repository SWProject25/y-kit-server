package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_qualification", indexes = {
        @Index(name = "idx_policy_id", columnList = "policy_id"),
        @Index(name = "idx_age", columnList = "sprt_trgt_min_age, sprt_trgt_max_age")
})
@Entity
public class PolicyQualificationEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    // 연령 요건
    @Column(name = "sprt_trgt_age_lmtt_yn")
    private String sprtTrgtAgeLmttYn;

    @Column(name = "sprt_trgt_min_age")
    private Integer sprtTrgtMinAge;

    @Column(name = "sprt_trgt_max_age")
    private Integer sprtTrgtMaxAge;

    // 소득 요건
    @Enumerated(EnumType.STRING)
    @Column(name = "earn_cnd_se_cd")
    private IncomeConditionType earnCndSeCd;

    @Column(name = "earn_min_amt", precision = 15, scale = 2)
    private BigDecimal earnMinAmt;

    @Column(name = "earn_max_amt", precision = 15, scale = 2)
    private BigDecimal earnMaxAmt;

    @Column(name = "earn_etc_cn", columnDefinition = "TEXT")
    private String earnEtcCn;

    // 결혼 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "mrg_stts_cd")
    private MaritalStatus mrgSttsCd;

    // 학력/취업/전공/특화 요건
    @Enumerated(EnumType.STRING)
    @Column(name = "school_cd")
    private EducationLevel schoolCd;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_cd")
    private EmploymentStatus jobCd;

    @Enumerated(EnumType.STRING)
    @Column(name = "plcy_major_cd")
    private MajorField plcyMajorCd;

    @Enumerated(EnumType.STRING)
    @Column(name = "sbiz_cd")
    private SpecializedRequirement sbizCd;

    @Column(name = "add_aply_qlfcn_cn", columnDefinition = "TEXT")
    private String addAplyQlfcnCn;

    // 비즈니스 메서드
    public boolean isAgeEligible(int age) {
        if (sprtTrgtMinAge == null && sprtTrgtMaxAge == null) {
            return true;
        }
        if (sprtTrgtMinAge != null && age < sprtTrgtMinAge) {
            return false;
        }
        if (sprtTrgtMaxAge != null && age > sprtTrgtMaxAge) {
            return false;
        }
        return true;
    }

    public boolean isIncomeEligible(BigDecimal income) {
        if (earnMinAmt == null && earnMaxAmt == null) {
            return true;
        }
        if (earnMinAmt != null && income.compareTo(earnMinAmt) < 0) {
            return false;
        }
        if (earnMaxAmt != null && income.compareTo(earnMaxAmt) > 0) {
            return false;
        }
        return true;
    }
}
