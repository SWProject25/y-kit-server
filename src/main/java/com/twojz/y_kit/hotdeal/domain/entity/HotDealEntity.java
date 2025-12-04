package com.twojz.y_kit.hotdeal.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "hot_deal")
public class HotDealEntity extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealType dealType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotDealCategory category;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private LocalDateTime expiresAt;

    private int viewCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int bookmarkCount = 0;

    @OneToMany(mappedBy = "hotDeal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotDealCommentEntity> comments;

    @Builder
    public HotDealEntity(UserEntity user, String title, String content, String placeName, String url, Double latitude, Double longitude, String address, DealType dealType, HotDealCategory category, Region region, LocalDateTime expiresAt) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.placeName = placeName;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.dealType = dealType;
        this.category = category;
        this.region = region;
        this.expiresAt = expiresAt;
    }

    public void update(String title, String content, String placeName, LocalDateTime expiresAt, DealType dealType, HotDealCategory category, Region region, String url, Double latitude, Double longitude, String address) {
        this.title = title;
        this.content = content;
        this.placeName = placeName;
        this.expiresAt = expiresAt;
        this.dealType = dealType;
        this.category = category;
        this.region = region;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
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

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
