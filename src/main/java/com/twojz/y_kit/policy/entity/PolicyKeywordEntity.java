package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_keyword", indexes = {
        @Index(name = "idx_keyword", columnList = "keyword")
})
@Entity
public class PolicyKeywordEntity extends BaseEntity {
    @Column(name = "keyword", unique = true, nullable = false)
    private String keyword;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    public void increaseUsageCount() {
        this.usageCount++;
    }
}
