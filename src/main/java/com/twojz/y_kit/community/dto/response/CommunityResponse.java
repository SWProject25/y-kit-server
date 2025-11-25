package com.twojz.y_kit.community.dto.response;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CommunityResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "정책 후기 공유합니다")
    private String title;

    @Schema(description = "내용", example = "청년정책 신청 절차 후기...")
    private String content;

    @Schema(description = "카테고리", example = "POLICY_REVIEW")
    private CommunityCategory category;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "조회수", example = "123")
    private int viewCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;
}
