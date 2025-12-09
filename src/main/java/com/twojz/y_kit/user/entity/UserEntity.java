package com.twojz.y_kit.user.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.region.entity.Region;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
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

    private String nickName;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @Enumerated(EnumType.STRING)
    private MajorField major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus profileStatus;

    @Builder
    public UserEntity(String email, String password, Role role, LoginProvider loginProvider,
                      String name, String nickName, String socialId,
                      LocalDate birthDate, Gender gender, Region region,
                      EmploymentStatus employmentStatus, EducationLevel educationLevel, MajorField major) {

        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.socialId = socialId;
        this.loginProvider = loginProvider;
        this.name = name;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.region = region;
        this.employmentStatus = employmentStatus;
        this.educationLevel = educationLevel;
        this.major = major;

        updateProfileStatus();
    }

    public void completeProfile(
            String name,
            String nickName,
            LocalDate birthDate,
            Gender gender,
            Region region,
            EmploymentStatus employmentStatus,
            EducationLevel educationLevel,
            MajorField major
    ) {
        if (name != null && !name.isEmpty()) this.name = name;
        if(nickName != null && !nickName.isEmpty()) this.nickName = nickName;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (region != null) this.region = region;
        if (employmentStatus != null) this.employmentStatus = employmentStatus;
        if (educationLevel != null) this.educationLevel = educationLevel;
        if (major != null) this.major = major;

        updateProfileStatus();
    }

    public void skipProfile() {
        this.profileStatus = ProfileStatus.SKIPPED;
    }

    private void updateProfileStatus() {
        boolean allFilled =
                this.name != null &&
                        this.birthDate != null &&
                        this.gender != null &&
                        this.region != null &&
                        this.employmentStatus != null &&
                        this.educationLevel != null &&
                        this.major != null;

        boolean noneFilled =
                this.name != null &&
                        this.birthDate == null &&
                        this.gender == null &&
                        this.region == null &&
                        this.employmentStatus == null &&
                        this.educationLevel == null &&
                        this.major == null;

        if (allFilled) {
            this.profileStatus = ProfileStatus.COMPLETED;
        } else if (noneFilled) {
            this.profileStatus = ProfileStatus.NOT_STARTED;
        } else {
            this.profileStatus = ProfileStatus.IN_PROGRESS;
        }
    }

    public int calculateAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}