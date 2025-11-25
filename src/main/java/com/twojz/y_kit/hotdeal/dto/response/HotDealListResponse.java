package com.twojz.y_kit.hotdeal.dto.response;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "이미지 URL", example = "https://example.com/hotdeal-list.jpg")
    private String url;

    @Schema(description = "좋아요 개수", example = "14")
    private long likeCount;

    @Schema(description = "댓글 개수", example = "3")
    private long commentCount;

    @Schema(description = "지역 코드", example = "1101053")
    private String regionCode;

    public static HotDealListResponse from(HotDealEntity hotDeal, long likeCount, long commentCount) {
        return HotDealListResponse.builder()
                .id(hotDeal.getId())
                .title(hotDeal.getTitle())
                .placeName(hotDeal.getPlaceName())
                .address(hotDeal.getAddress())
                .url(hotDeal.getUrl())
                .regionCode(hotDeal.getRegion().getCode())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }

    public static HotDealListResponse fromDto(HotDealWithCountsDto dto) {
        return HotDealListResponse.builder()
                .id(dto.hotDealId())
                .title(dto.title())
                .placeName(dto.placeName())
                .address(dto.address())
                .url(dto.url())
                .regionCode(dto.regionCode())
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .build();
    }
}
