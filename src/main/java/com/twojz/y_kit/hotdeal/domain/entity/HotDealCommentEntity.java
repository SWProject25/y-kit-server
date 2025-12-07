package com.twojz.y_kit.hotdeal.domain.entity;

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
@Table(name = "hot_deal_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class HotDealCommentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hot_deal_id")
    private HotDealEntity hotDeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Builder
    public HotDealCommentEntity(HotDealEntity hotDeal, UserEntity user, String content) {
        this.hotDeal = hotDeal;
        this.user = user;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
