package com.twojz.y_kit.hotdeal.dto.response;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealDetailDto;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "핫딜 상세 조회 응답")
public class HotDealDetailResponse {
    @Schema(description = "핫딜 ID", example = "1")
    private Long id;

    @Schema(description = "핫딜 제목", example = "파리바게뜨 빵 50% 할인")
    private String title;

    @Schema(description = "상세 설명", example = "전 품목 50% 할인! 선착순 100명")
    private String content;

    @Schema(description = "가게명", example = "파리바게뜨 홍대점")
    private String placeName;

    @Schema(description = "주소", example = "서울 마포구 양화로 100")
    private String address;

    @Schema(description = "이미지 URL", example = "https://example.com/hotdeal.jpg")
    private String url;

    @Schema(description = "위도", example = "37.5662952")
    private Double latitude;

    @Schema(description = "경도", example = "126.9779451")
    private Double longitude;

    @Schema(description = "딜 타입", example = "DISCOUNT")
    private DealType dealType;

    @Schema(description = "카테고리", example = "FOOD")
    private HotDealCategory category;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "좋아요 여부", example = "true")
    private boolean isLiked;

    @Schema(description = "북마크 여부", example = "false")
    private boolean isBookmarked;

    @Schema(description = "좋아요 개수", example = "23")
    private long likeCount;

    @Schema(description = "댓글 개수", example = "7")
    private long commentCount;

    @Schema(description = "조회수", example = "456")
    private int viewCount;

    @Schema(description = "지역명", example = "서울특별시 종로구")
    private String regionName;

    @Schema(description = "만료 일시", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "작성일", example = "2024-12-01T14:22:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-12-01T15:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "댓글 목록")
    private List<HotDealCommentResponse> comments;

    public static HotDealDetailResponse from(HotDealEntity hotDeal, boolean isLiked, boolean isBookmarked, long likeCount, long commentCount, List<HotDealCommentResponse> comments) {
        return HotDealDetailResponse.builder()
                .id(hotDeal.getId())
                .title(hotDeal.getTitle())
                .content(hotDeal.getContent())
                .placeName(hotDeal.getPlaceName())
                .address(hotDeal.getAddress())
                .url(hotDeal.getUrl())
                .latitude(hotDeal.getLatitude())
                .longitude(hotDeal.getLongitude())
                .dealType(hotDeal.getDealType())
                .category(hotDeal.getCategory())
                .authorId(hotDeal.getUser().getId())
                .authorName(hotDeal.getUser().getName())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .viewCount(hotDeal.getViewCount())
                .regionName(hotDeal.getRegion() != null ? hotDeal.getRegion().getFullName() : null)
                .expiresAt(hotDeal.getExpiresAt())
                .createdAt(hotDeal.getCreatedAt())
                .updatedAt(hotDeal.getUpdatedAt())
                .comments(comments)
                .build();
    }
}