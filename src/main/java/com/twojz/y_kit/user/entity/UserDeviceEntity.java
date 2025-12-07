package com.twojz.y_kit.user.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "user_device",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_token", columnNames = {"device_token"})
        }
)
public class UserDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String deviceName;

    @Column(nullable = false)
    private String deviceToken;

    @Column(unique = true, length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserDeviceEntity(UserEntity user, String deviceName, String deviceToken,
                            String refreshToken, LocalDateTime refreshTokenExpiresAt,
                            Boolean isActive, Boolean notificationEnabled, LocalDateTime lastLogin) {
        this.user = user;
        this.deviceName = deviceName;
        this.deviceToken = deviceToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.isActive = isActive != null ? isActive : true;
        this.notificationEnabled = notificationEnabled != null ? notificationEnabled : true;
        this.lastLogin = lastLogin;
    }

    public void updateLoginInfo(String deviceName, String deviceToken) {
        this.deviceName = deviceName;
        this.deviceToken = deviceToken;
        this.isActive = true;
        this.lastLogin = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
        this.lastLogin = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt == null ||
                LocalDateTime.now().isAfter(refreshTokenExpiresAt);
    }

    public void enableNotification() {
        this.notificationEnabled = true;
    }

    public void disableNotification() {
        this.notificationEnabled = false;
    }
}