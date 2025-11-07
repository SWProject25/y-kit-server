package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

import java.util.ArrayList;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policy_category")
@Entity
public class PolicyCategoryEntity extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PolicyCategoryEntity parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<PolicyCategoryEntity> children = new ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;
}
