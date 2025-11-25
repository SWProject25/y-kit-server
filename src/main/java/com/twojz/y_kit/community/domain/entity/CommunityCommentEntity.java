package com.twojz.y_kit.community.domain.entity;

import com.twojz.y_kit.global.entity.BaseEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "community_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CommunityCommentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private CommunityEntity community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public CommunityCommentEntity(CommunityEntity community, UserEntity user, String content) {
        this.community = community;
        this.user = user;
        this.content = content;
    }
}
