package com.twojz.y_kit.hotdeal.dto.response;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "좋아요 여부", example = "true")
    private boolean isLiked;

    @Schema(description = "북마크 여부", example = "false")
    private boolean isBookmarked;

    @Schema(description = "좋아요 개수", example = "23")
    private long likeCount;

    @Schema(description = "댓글 개수", example = "7")
    private long commentCount;

    @Schema(description = "댓글 목록")
    private List<HotDealCommentResponse> comments;

    public static HotDealDetailResponse from(HotDealEntity hotDeal, boolean isLiked, boolean isBookmarked, long likeCount, long commentCount, List<HotDealCommentResponse> comments) {
        return HotDealDetailResponse.builder()
                .id(hotDeal.getId())
                .title(hotDeal.getTitle())
                .placeName(hotDeal.getPlaceName())
                .address(hotDeal.getAddress())
                .url(hotDeal.getUrl())
                .latitude(hotDeal.getLatitude())
                .longitude(hotDeal.getLongitude())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .comments(comments)
                .build();
    }
}
