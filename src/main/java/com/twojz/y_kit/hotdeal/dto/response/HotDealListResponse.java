package com.twojz.y_kit.hotdeal.dto.response;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "핫딜 목록 조회 응답")
public class HotDealListResponse {
    @Schema(description = "핫딜 ID", example = "1")
    private Long id;

    @Schema(description = "핫딜 제목", example = "던킨도너츠 30% 할인")
    private String title;

    @Schema(description = "가게명", example = "던킨 합정점")
    private String placeName;

    @Schema(description = "주소", example = "서울 마포구 독막로 12")
    private String address;

    @Schema(description = "딜 타입", example = "DISCOUNT")
    private DealType dealType;

    @Schema(description = "카테고리", example = "FOOD")
    private HotDealCategory category;

    @Schema(description = "좋아요 개수", example = "14")
    private long likeCount;

    @Schema(description = "댓글 개수", example = "3")
    private long commentCount;

    @Schema(description = "좋아요 여부", example = "true")
    private boolean liked;

    @Schema(description = "북마크 여부", example = "false")
    private boolean bookmarked;

    @Schema(description = "조회수", example = "120")
    private int viewCount;

    @Schema(description = "지역명", example = "서울특별시 종로구")
    private String regionName;

    @Schema(description = "작성일", example = "2024-12-01T14:22:00")
    private LocalDateTime createdAt;

    @Schema(description = "만료 일시", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;

    /**
     * HotDealListDto → HotDealListResponse 변환
     */
    public static HotDealListResponse fromListDto(HotDealListDto dto) {
        return HotDealListResponse.builder()
                .id(dto.hotDealId())
                .title(dto.title())
                .placeName(dto.placeName())
                .address(dto.address())
                .dealType(dto.dealType())
                .category(dto.category())
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .liked(dto.isLiked())
                .bookmarked(dto.isBookmarked())
                .viewCount(dto.viewCount())
                .regionName(dto.regionName())
                .createdAt(dto.createdAt())
                .expiresAt(dto.expiresAt())
                .build();
    }
}