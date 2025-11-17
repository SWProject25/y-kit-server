package com.twojz.y_kit.group.domain.entity;

import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class GroupPurchaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모집자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String productName;

    private String productLink;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer minParticipants;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer currentParticipants = 0;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupPurchaseStatus status = GroupPurchaseStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder
    public GroupPurchaseEntity(UserEntity user, String title, String productName, String productLink,
                               BigDecimal price, Integer minParticipants, Integer maxParticipants,
                               LocalDateTime deadline, GroupPurchaseStatus status, Region region) {
        this.user = user;
        this.title = title;
        this.productName = productName;
        this.productLink = productLink;
        this.price = price;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.status = status != null ? status : GroupPurchaseStatus.OPEN;
        this.region = region;
    }

    public void increaseParticipants() {
        this.currentParticipants += 1;
    }

    public void update(String title, String productName, String productLink,
                       BigDecimal price, Integer minParticipants, Integer maxParticipants,
                       LocalDateTime deadline, GroupPurchaseStatus status, Region region) {
        this.title = title;
        this.productName = productName;
        this.productLink = productLink;
        this.price = price;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.status = status;
        this.region = region;
    }
}