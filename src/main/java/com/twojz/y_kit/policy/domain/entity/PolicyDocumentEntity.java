package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.dto.request.PolicyDocumentUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_document")
@Entity
public class PolicyDocumentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String documentsOriginal;

    @Column(columnDefinition = "JSON")
    private String documentsParsed;  // JSON 또는 줄바꿈 구분

    @Builder.Default
    private Boolean isRequired = true;

    public void updateFromApi(PolicyDocumentUpdateRequest dto) {
        this.documentsOriginal = dto.getDocumentsOriginal();
        this.isRequired = dto.isRequired();
    }

    public void updateOriginal(String documentsOriginal) {
        this.documentsOriginal = documentsOriginal;
    }

    /**
     * AI 파싱 결과 업데이트 (별도 메서드)
     */
    public void updateParsed(String documentsParsed) {
        this.documentsParsed = documentsParsed;
    }
}
