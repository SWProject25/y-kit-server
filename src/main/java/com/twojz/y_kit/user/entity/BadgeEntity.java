package com.twojz.y_kit.user.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BadgeEntity extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    public void update(String name, String description, String iconUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (iconUrl != null) {
            this.iconUrl = iconUrl;
        }
    }
}