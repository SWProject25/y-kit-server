package com.twojz.y_kit.group.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_purchase")
@Entity
public class GroupPurchaseEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String productLink;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contact;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer minParticipants;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentParticipants = 0;

    @Column(nullable = false)
    private LocalDate deadline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupPurchaseStatus status = GroupPurchaseStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    public void update(String title, String content, String productName, String productLink, String contact,
                       BigDecimal price, Integer minParticipants, Integer maxParticipants,
                       LocalDate deadline, Region region) {
        this.title = title;
        this.content = content;
        this.productName = productName;
        this.productLink = productLink;
        this.contact = contact;
        this.price = price;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.region = region;
    }

    public void increaseParticipants() {
        this.currentParticipants += 1;
    }
}