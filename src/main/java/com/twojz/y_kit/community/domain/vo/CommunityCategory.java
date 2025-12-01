package com.twojz.y_kit.community.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityCategory {
    POLICY_REVIEW("정책 후기"),
    LOCAL_NEWS("지역 소식"),
    QUESTION("질문·답변"),
    MEETUP("모임"),
    FREE("자유 게시판"),
    INFO("정보 공유"),
    NOTICE("공지사항"),
    EVENT("이벤트");

    private final String description;

    public static CommunityCategory of(String description) {
        try {
            return CommunityCategory.valueOf(description);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + description);
        }
    }
}
