package com.twojz.y_kit.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    WELCOME("환영"),
    PROFILE_COMPLETE_REMINDER("프로필 완성 요청"),
    BADGE("뱃지"),
    USER("사용자"),
    POLICY("정책"),
    HOT_DEAL("핫딜"),
    GROUP_BUYING("공동구매"),
    COMMUNITY("커뮤니티"),
    COMMENT("댓글"),
    SYSTEM("시스템");

    private final String description;
}