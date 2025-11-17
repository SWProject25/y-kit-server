package com.twojz.y_kit.hotdeal.dto.response;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotDealCommentResponse {
    @Schema(description = "댓글 ID", example = "12")
    private Long id;

    @Schema(description = "작성자 ID", example = "5")
    private Long userId;

    @Schema(description = "댓글 내용", example = "정말 득템했어요!")
    private String content;

    @Schema(description = "작성일", example = "2024-12-01T14:22:00")
    private LocalDateTime createdAt;

    public static HotDealCommentResponse from(HotDealCommentEntity comment) {
        return HotDealCommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
