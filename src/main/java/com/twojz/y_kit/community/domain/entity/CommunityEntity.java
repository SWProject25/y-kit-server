package com.twojz.y_kit.community.domain.entity;

import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community")
@Entity
public class CommunityEntity extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Builder
    public CommunityEntity(String title, String content, CommunityCategory category, UserEntity user) {
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.category = category;
        this.user = user;
    }

    public void increaseViewCount() {
        viewCount++;
    }

    public void update(String title, String content, CommunityCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}
