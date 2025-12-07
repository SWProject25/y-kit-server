package com.twojz.y_kit.global.controller;

import com.twojz.y_kit.global.dto.GlobalSearchResponse;
import com.twojz.y_kit.global.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "전체 통합 검색 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @Operation(
            summary = "전체 통합 검색",
            description = "정책, 핫딜, 공동구매, 커뮤니티를 한 번에 검색합니다. 각 도메인별로 독립적인 페이지네이션이 적용됩니다."
    )
    @GetMapping
    public ResponseEntity<GlobalSearchResponse> globalSearch(
            @RequestParam @Size(min = 2, message = "검색어는 최소 2자 이상이어야 합니다") String keyword,
            Authentication authentication,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        GlobalSearchResponse response = globalSearchService.searchAll(keyword, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 선택 - userId 추출 (로그인 안되어 있으면 null 반환)
     */
    private Long extractUserIdOrNull(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if ("anonymousUser".equals(principal)) {
            return null;
        }

        try {
            String name = authentication.getName();
            if (name == null || name.isEmpty() || "anonymousUser".equals(name)) {
                return null;
            }
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            // 파싱 실패 시 그냥 null 반환 (예외 발생 안함)
            return null;
        }
    }
}
