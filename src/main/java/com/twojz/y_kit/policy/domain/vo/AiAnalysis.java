package com.twojz.y_kit.policy.domain.vo;

import java.util.List;
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
    private String summary;
    private List<String> advantages;
    private List<String> disadvantages;
}
