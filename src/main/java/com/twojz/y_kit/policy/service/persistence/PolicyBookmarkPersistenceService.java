package com.twojz.y_kit.policy.service.persistence;

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
public class PolicyBookmarkPersistenceService {

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

    public List<PolicyBookmarkEntity> findByUserWithDetailOrderByCreatedAtDesc(UserEntity user) {
        return policyBookmarkRepository.findByUserWithDetailOrderByCreatedAtDesc(user);
    }

    @Transactional
    public PolicyBookmarkEntity save(PolicyBookmarkEntity bookmark) {
        return policyBookmarkRepository.save(bookmark);
    }

    @Transactional
    public void delete(PolicyBookmarkEntity bookmark) {
        policyBookmarkRepository.delete(bookmark);
    }

    @Transactional
    public void deleteByUser(UserEntity user) {
        policyBookmarkRepository.deleteByUser(user);
    }
}
