package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.AiAnalysisConverter;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
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

    public void updateAiAnalysis(AiAnalysis aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
        this.aiGeneratedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
