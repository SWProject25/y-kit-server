package com.twojz.y_kit.hotdeal.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type")
    private DealType dealType;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    private String url;

    private Double latitude;
    private Double longitude;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private LocalDateTime expiresAt;

    private int viewCount = 0;

    @Builder
    public HotDealEntity(UserEntity user, String title, String placeName, String url, Double latitude, Double longitude, String address, DealType dealType, Region region, LocalDateTime expiresAt) {
        this.user = user;
        this.title = title;
        this.placeName = placeName;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.dealType = dealType;
        this.region = region;
        this.expiresAt = expiresAt;
    }

    public void update(String title, String placeName, LocalDateTime expiresAt, DealType dealType, Region region, String url, Double latitude, Double longitude, String address) {
        this.title = title;
        this.placeName = placeName;
        this.expiresAt = expiresAt;
        this.dealType = dealType;
        this.region = region;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public void increaseViewCount() {
        viewCount++;
    }
}
