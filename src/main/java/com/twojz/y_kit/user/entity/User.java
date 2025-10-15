package com.twojz.y_kit.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_provider", nullable = false)
    private LoginProvider loginProvider;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Long regionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @Builder
    public User(String email, String password, String name, Role role, LoginProvider loginProvider, Integer age, Gender gender, Long regionId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.loginProvider = loginProvider;
        this.age = age;
        this.gender = gender;
        this.regionId = regionId;
    }
}