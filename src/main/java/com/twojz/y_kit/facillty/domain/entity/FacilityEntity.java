package com.twojz.y_kit.facillty.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "facilities",
        indexes = {
                @Index(name = "idx_facility_lat_lng", columnList = "latitude, longitude"),
                @Index(name = "idx_facility_category", columnList = "category")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String resourceNo;

    @Column(nullable = false)
    private String name;

    private String zipCode;

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String imgUrl;
    private String reservationUrl;

    @Enumerated(EnumType.STRING)
    private FacilityCategory category;

    @Builder
    public FacilityEntity(String resourceNo, String name, String zipCode, String address, String detailAddress, Double latitude, Double longitude, String imgUrl, String reservationUrl, FacilityCategory category ) {
        this.resourceNo = resourceNo;
        this.name = name;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imgUrl = imgUrl;
        this.reservationUrl = reservationUrl;
        this.category = category;
    }

    public void update(FacilityEntity source) {
        this.resourceNo = source.resourceNo;
        this.name = source.name;
        this.zipCode = source.zipCode;
        this.address = source.address;
        this.detailAddress = source.detailAddress;
        this.latitude = source.latitude;
        this.longitude = source.longitude;
        this.imgUrl = source.imgUrl;
        this.reservationUrl = source.reservationUrl;
        this.category = source.category;
    }
}
