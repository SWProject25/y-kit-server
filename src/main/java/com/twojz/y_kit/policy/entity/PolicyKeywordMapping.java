package com.twojz.y_kit.policy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policy_keyword_mapping", indexes = {
        @Index(name = "idx_policy_keyword", columnList = "policy_id, keyword_id", unique = true),
        @Index(name = "idx_keyword_id", columnList = "keyword_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PolicyKeywordMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private PolicyKeywordEntity keyword;
}
