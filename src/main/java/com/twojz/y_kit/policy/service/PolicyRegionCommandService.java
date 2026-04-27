package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.repository.PolicyRegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyRegionCommandService {
    private final PolicyRegionRepository policyRegionRepository;

    public void deleteAll(List<PolicyRegion> mappings) {
        if (!mappings.isEmpty()) {
            policyRegionRepository.deleteAll(mappings);
        }
    }

    public void saveAll(List<PolicyRegion> mappings) {
        if (!mappings.isEmpty()) {
            policyRegionRepository.saveAll(mappings);
        }
    }
}
