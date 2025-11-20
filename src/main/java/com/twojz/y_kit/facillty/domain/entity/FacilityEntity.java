package com.twojz.y_kit.facillty.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name= "facilities")
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
}
