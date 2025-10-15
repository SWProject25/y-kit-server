package com.twojz.y_kit.user;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

    @Entity
    @Table(name = "user")
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

        protected User() {}

        public User(String email, String password, String name, Role role, LoginProvider loginProvider) {
            this.email = email;
            this.password = password;
            this.name = name;
            this.role = role;
            this.loginProvider = loginProvider;
            this.age = null;
            this.gender = null;
            this.regionId = null;
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public Role getRole() {
            return role;
        }

        public String getSocialId() {
            return socialId;
        }

        public LoginProvider getLoginProvider() {
            return loginProvider;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public Gender getGender() {
            return gender;
        }

        public Long getRegionId() {
            return regionId;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public enum Role {
            ADMIN, USER
        }

        public enum LoginProvider {
            LOCAL, KAKAO, GOOGLE
        }

        public enum Gender {
            MAN, WOMAN
        }
    }


