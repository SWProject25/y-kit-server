package com.twojz.y_kit.user.entity;

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

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_token", nullable = false, unique = true)
    private String deviceToken;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserDeviceEntity(UserEntity user,
                            String deviceName,
                            String deviceToken,
                            Boolean isActive,
                            LocalDateTime lastLogin) {
        this.user = user;
        this.deviceName = deviceName;
        this.deviceToken = deviceToken;
        this.isActive = isActive != null ? isActive : true;
        this.lastLogin = lastLogin;
    }

    public void updateLoginInfo(String deviceName, String deviceToken) {
        this.deviceName = deviceName;
        this.deviceToken = deviceToken;
        this.isActive = true;
        this.lastLogin = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }
}