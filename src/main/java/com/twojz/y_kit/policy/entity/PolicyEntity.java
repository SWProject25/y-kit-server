package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies", indexes = {
        @Index(name = "idx_policy_no", columnList = "policy_no"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Entity
public class PolicyEntity extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String policyNo;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer bookmarkCount = 0;

    @Builder.Default
    private Integer applicationCount = 0;

    @Builder.Default
    private Boolean isActive = true;

    // 연관 관계
    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PolicyDetailEntity detail;

    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PolicyQualificationEntity qualification;

    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PolicyApplicationEntity application;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyDocumentEntity> documents = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyRegion> policyRegions = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyCategoryMapping> categoryMappings = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyKeywordMapping> keywordMappings = new ArrayList<>();

    // 비즈니스 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

    public void increaseApplicationCount() {
        this.applicationCount++;
    }

    public void setApplication(PolicyApplicationEntity application) {
        this.application = application;
    }

    public void setDetail(PolicyDetailEntity detail) {
        this.detail = detail;
    }

    public void setQualification(PolicyQualificationEntity qualification) {
        this.qualification = qualification;
    }
}
