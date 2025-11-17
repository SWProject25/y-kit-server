package com.twojz.y_kit.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupPurchaseCommentResponse {
    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "42")
    private Long userId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String username;

    @Schema(description = "댓글 내용", example = "저도 참여하고 싶어요!")
    private String content;

    @Schema(description = "댓글 작성 시간", example = "2025-11-17T14:30:00")
    private LocalDateTime createdAt;

    public static GroupPurchaseCommentResponse from(com.twojz.y_kit.group.domain.entity.GroupPurchaseCommentEntity comment) {
        return GroupPurchaseCommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
