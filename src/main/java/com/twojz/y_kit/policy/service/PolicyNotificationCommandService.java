package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.policy.repository.PolicyNotificationRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyNotificationCommandService {
    private final PolicyNotificationRepository policyNotificationRepository;

    public PolicyNotificationEntity save(PolicyNotificationEntity notification) {
        return policyNotificationRepository.save(notification);
    }

    public void delete(PolicyNotificationEntity notification) {
        policyNotificationRepository.delete(notification);
    }

    public void deleteByPolicyAndUser(PolicyEntity policy, UserEntity user) {
        policyNotificationRepository.deleteByPolicyAndUser(policy, user);
    }

    public void deleteByUser(UserEntity user) {
        policyNotificationRepository.deleteByUser(user);
    }
}
