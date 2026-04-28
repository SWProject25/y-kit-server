package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyBookmarkFindService {
    private final PolicyBookmarkRepository policyBookmarkRepository;

    public boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user) {
        return policyBookmarkRepository.existsByPolicyAndUser(policy, user);
    }

    public Optional<PolicyBookmarkEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user) {
        return policyBookmarkRepository.findByPolicyAndUser(policy, user);
    }

    public List<Long> findBookmarkedPolicyIdsByUserAndPolicyIds(UserEntity user, List<Long> policyIds) {
        return policyBookmarkRepository.findBookmarkedPolicyIdsByUserAndPolicyIds(user, policyIds);
    }

    public List<PolicyBookmarkEntity> findByUserOrderByCreatedAtDesc(UserEntity user) {
        return policyBookmarkRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
