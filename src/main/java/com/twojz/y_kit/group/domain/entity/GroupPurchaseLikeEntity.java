package com.twojz.y_kit.group.domain.entity;

import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor
public class GroupPurchaseLikeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private GroupPurchaseEntity groupPurchase;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    private LocalDateTime createdAt = LocalDateTime.now();
}
