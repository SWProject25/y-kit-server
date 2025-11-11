package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.enumType.*;
import com.twojz.y_kit.policy.dto.request.PolicyQualificationUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_qualification")
@Entity
public class PolicyQualificationEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    // 연령 요건
    private String sprtTrgtAgeLmtYn;                            // 지원대상연령제한여부
    private Integer sprtTrgtMinAge;                             // 지원대상최소연령
    private Integer sprtTrgtMaxAge;                             // 지원대상최대연령

    // 소득 요건
    @Enumerated(EnumType.STRING)
    @Column(name = "earn_cnd_se_cd")
    private IncomeConditionType earnCndSeCd;                    // 소득조건구분코드

    @Column(name = "earn_min_amt", precision = 15, scale = 0)
    private BigDecimal earnMinAmt;                              // 소득최소금액

    @Column(name = "earn_max_amt", precision = 15, scale = 0)
    private BigDecimal earnMaxAmt;                              // 소득최대금액

    @Column(name = "earn_etc_cn", columnDefinition = "TEXT")
    private String earnEtcCn;                                   // 소득기타내용

    // 결혼 상태
    @Enumerated(EnumType.STRING)
    private MaritalStatus mrgSttsCd;                            // 결혼상태코드

    // 학력/취업/전공/특화 요건
    @Enumerated(EnumType.STRING)
    private EducationLevel schoolCd;                            // 정책학력요건코드

    @Enumerated(EnumType.STRING)
    private EmploymentStatus jobCd;                             // 정책취업요건콛,

    @Enumerated(EnumType.STRING)
    private MajorField plcyMajorCd;                             // 정책전공요건코드

    @Enumerated(EnumType.STRING)
    private SpecializedRequirement sbizCd;                      // 정책특화요건코드

    @Column(columnDefinition = "TEXT")
    private String addAplyQlfcCndCn;                            // 추가신청자격조건내용

    public void updateFromApi(PolicyQualificationUpdateRequest dto) {
        this.sprtTrgtAgeLmtYn = dto.getSprtTrgtAgeLmtYn();
        this.sprtTrgtMinAge = dto.getSprtTrgtMinAge();
        this.sprtTrgtMaxAge = dto.getSprtTrgtMaxAge();
        this.earnCndSeCd = dto.getEarnCndSeCd();
        this.earnMinAmt = dto.getEarnMinAmt();
        this.earnMaxAmt = dto.getEarnMaxAmt();
        this.earnEtcCn = dto.getEarnEtcCn();
        this.mrgSttsCd = dto.getMrgSttsCd();
        this.schoolCd = dto.getSchoolCd();
        this.jobCd = dto.getJobCd();
        this.plcyMajorCd = dto.getPlcyMajorCd();
        this.sbizCd = dto.getSBizCd();
        this.addAplyQlfcCndCn = dto.getAddAplyQlfcCndCn();
    }

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
