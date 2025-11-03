package com.twojz.y_kit.policy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policy_category_mapping", indexes = {
        @Index(name = "idx_policy_category", columnList = "policy_id, category_id", unique = true),
        @Index(name = "idx_category_id", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCategoryMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PolicyCategoryEntity category;
}
