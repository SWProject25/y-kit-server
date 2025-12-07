package com.twojz.y_kit.community.dto.response;

import com.twojz.y_kit.community.domain.entity.CommunityCommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!")
    private String content;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "김철수")
    private String authorName;

    @Schema(description = "작성일시", example = "2024-11-08T10:30:00")
    private LocalDateTime createdAt;

    public static CommentResponse from(CommunityCommentEntity entity) {
        return CommentResponse.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .authorId(entity.getUser().getId())
                .authorName(entity.getUser().getNickName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
