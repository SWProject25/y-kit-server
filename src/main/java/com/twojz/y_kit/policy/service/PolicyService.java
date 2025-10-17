package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.external.policy.dto.YouthPolicyResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final YouthPolicyClient youthPolicyClient;

    public Mono<List<YouthPolicy>> fetchPoliciesFromApi() {
        return youthPolicyClient.fetchAllPolicies();
    }
}
