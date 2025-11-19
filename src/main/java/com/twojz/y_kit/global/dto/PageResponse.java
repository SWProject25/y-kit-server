package com.twojz.y_kit.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 페이징 응답 DTO
 * 프론트엔드와 일관성을 위해 page는 0부터 시작
 */
@Getter
@Schema(description = "페이징 응답")
public class PageResponse<T> {
    @Schema(description = "데이터 목록")
    private final List<T> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private final int page;

    @Schema(description = "페이지 크기", example = "20")
    private final int size;

    @Schema(description = "전체 데이터 수", example = "100")
    private final long totalElements;

    @Schema(description = "전체 페이지 수", example = "5")
    private final int totalPages;

    @Schema(description = "마지막 페이지 여부")
    private final boolean last;

    @Schema(description = "첫 페이지 여부")
    private final boolean first;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();  // 0부터 시작 (Spring과 동일)
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
    }
}