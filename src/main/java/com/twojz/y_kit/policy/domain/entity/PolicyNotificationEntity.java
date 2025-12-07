package com.twojz.y_kit.policy.domain.entity;

import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "policy_notifications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "user_id"})
)
@Entity
public class PolicyNotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Builder.Default
    private boolean notificationSent = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public void markAsSent() {
        this.notificationSent = true;
    }
}
