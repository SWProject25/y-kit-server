package com.twojz.y_kit.policy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyComparisonResponse {
    @JsonProperty("recommended_policy")
    private PolicyRecommendation recommendedPolicy;

    private List<PolicyRecommendation> alternatives;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyRecommendation {
        private String name;

        @JsonProperty("fit_score")
        private Double fitScore;

        private String reason;
    }
}

