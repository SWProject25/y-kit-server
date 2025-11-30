package com.twojz.y_kit.user.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.region.entity.Region;
import jakarta.persistence.*;
import java.time.LocalDate;
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

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus profileStatus;

    @Builder
    public UserEntity(String email, String password, Role role, LoginProvider loginProvider,
                      String name, String socialId, LocalDate birthDate, Gender gender, Region region) {
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.socialId = socialId;
        this.loginProvider = loginProvider;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.region = region;

        updateProfileStatus();
    }

    public void completeProfile(String name, LocalDate birthDate, Gender gender, Region region) {
        if (name != null) this.name = name;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (region != null) this.region = region;

        updateProfileStatus();
    }

    private void updateProfileStatus() {
        boolean allFilled = this.birthDate != null &&
                this.gender != null && this.region != null;
        boolean noneFilled = this.birthDate == null &&
                this.gender == null && this.region == null;

        if (allFilled) {
            this.profileStatus = ProfileStatus.COMPLETED;
        } else if (noneFilled) {
            this.profileStatus = ProfileStatus.NOT_STARTED;
        } else {
            this.profileStatus = ProfileStatus.IN_PROGRESS;
        }
    }
}