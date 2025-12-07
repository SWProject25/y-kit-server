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

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String address;

    @Enumerated(EnumType.STRING)
    @Column
    private GroupPurchaseCategory category;

    @Builder.Default
    @Column
    private int viewCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int bookmarkCount = 0;

    public void update(String title, String content, String productName, String productLink, String contact,
                       BigDecimal price, Integer minParticipants, Integer maxParticipants,
                       LocalDate deadline, Region region, Double latitude, Double longitude, String address, GroupPurchaseCategory category) {
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
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.category = category;
    }

    public void increaseParticipants() {
        this.currentParticipants += 1;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }
}