package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "정책 기본 정보")
public class PolicyBasicInfo {
    @Schema(description = "정책 ID", example = "1")
    private Long policyId;

    @Schema(description = "정책 번호", example = "R2024012345678")
    private String policyNo;

    @Schema(description = "활성화 여부")
    private Boolean isActive;

    @Schema(description = "조회수", example = "1234")
    private Integer viewCount;

    @Schema(description = "북마크 수", example = "56")
    private Integer bookmarkCount;

    @Schema(description = "신청 수", example = "789")
    private Integer applicationCount;

    @Schema(description = "등록일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static PolicyBasicInfo from(PolicyEntity entity) {
        return PolicyBasicInfo.builder()
                .policyId(entity.getId())
                .policyNo(entity.getPolicyNo())
                .isActive(entity.getIsActive())
                .viewCount(entity.getViewCount())
                .bookmarkCount(entity.getBookmarkCount())
                .applicationCount(entity.getApplicationCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
