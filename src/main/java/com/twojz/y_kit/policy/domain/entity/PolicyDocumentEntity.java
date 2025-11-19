package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_document")
@Entity
public class PolicyDocumentEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private PolicyEntity policy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String documentsOriginal;

    @Column(columnDefinition = "JSON")
    private String documentsParsed;

    public void updateOriginal(String documentsOriginal) {
        this.documentsOriginal = documentsOriginal;
    }

    public void updateParsed(String documentsParsed) {
        this.documentsParsed = documentsParsed;
    }
}
