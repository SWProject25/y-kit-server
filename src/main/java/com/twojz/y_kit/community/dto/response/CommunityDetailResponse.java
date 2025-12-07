package com.twojz.y_kit.community.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityDetailResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "청년 정책 후기 공유합니다")
    private String title;

    @Schema(description = "게시글 내용", example = "이번에 청년 주거 지원 정책을 신청해봤는데...")
    private String content;

    @Schema(description = "카테고리", example = "POLICY_REVIEW")
    private CommunityCategory category;

    @Schema(description = "카테고리 설명", example = "정책 후기")
    private String categoryDescription;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorName;

    @Schema(description = "조회수", example = "150")
    private int viewCount;

    @Schema(description = "좋아요 수", example = "25")
    private long likeCount;

    @Schema(description = "댓글 수", example = "10")
    private long commentCount;

    @Schema(description = "좋아요 여부", example = "true")
    @JsonProperty("isLiked")
    private boolean isLiked;

    @Schema(description = "북마크 여부", example = "false")
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    @Schema(description = "댓글 목록")
    private List<CommentResponse> comments;

    @Schema(description = "작성일시", example = "2024-11-08T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-11-08T11:00:00")
    private LocalDateTime updatedAt;

    public static CommunityDetailResponse from(
            CommunityEntity entity,
            boolean isLiked,
            boolean isBookmarked,
            long likeCount,
            long commentCount,
            List<CommentResponse> comments
    ) {
        return CommunityDetailResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getCategory())
                .categoryDescription(entity.getCategory().getDescription())
                .authorId(entity.getUser().getId())
                .authorName(entity.getUser().getNickName())
                .viewCount(entity.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .comments(comments)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}