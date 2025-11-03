package com.twojz.y_kit.policy.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_document", indexes = {
        @Index(name = "idx_policy_id", columnList = "policy_id")
})
@Entity
public class PolicyDocumentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String documentsOriginal;

    @Builder.Default
    private Boolean isRequired = true;
}
