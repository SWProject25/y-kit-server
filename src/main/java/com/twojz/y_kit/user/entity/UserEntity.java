package com.twojz.y_kit.user.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.region.entity.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class UserEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginProvider loginProvider;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Builder
    public UserEntity(String email, String password, Role role, LoginProvider loginProvider,
                      String name, String socialId, Integer age, Gender gender, Region region) {
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.socialId = socialId;
        this.loginProvider = loginProvider;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.region = region;
    }
}