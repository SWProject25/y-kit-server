package com.twojz.y_kit.policy.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAnalysis {
    private String summary;         // 요약
    private String pros;            // 장점
    private String cons;            // 단점
    private String recommendation;  // 추천 대상
}
