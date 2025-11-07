package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.AiAnalysisConverter;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies")
@Entity
public class PolicyEntity extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String policyNo;

    private Integer viewCount;

    private Integer bookmarkCount;

    private Integer applicationCount;

    private Boolean isActive;

    @Convert(converter = AiAnalysisConverter.class)
    @Column(columnDefinition = "JSON")
    private AiAnalysis aiAnalysis;

    private LocalDateTime aiGeneratedAt;

    // 양방향 관계 추가
    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PolicyDetailEntity detail;

    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PolicyApplicationEntity application;

    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PolicyQualificationEntity qualification;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyDocumentEntity> documents = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyCategoryMapping> categoryMappings = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyKeywordMapping> keywordMappings = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PolicyRegion> regions = new ArrayList<>();

    @Builder
    public PolicyEntity(String policyNo, Boolean isActive) {
        this.policyNo = policyNo;
        this.viewCount = 0;
        this.bookmarkCount = 0;
        this.applicationCount = 0;
        this.isActive = isActive != null ? isActive : true;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void increaseApplicationCount() {
        this.applicationCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }
}
