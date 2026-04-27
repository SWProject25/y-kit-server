package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.policy.dto.response.PolicyNotificationResponse;
import com.twojz.y_kit.policy.repository.PolicyNotificationQueryRepository.PendingPolicyNotification;
import com.twojz.y_kit.policy.repository.PolicyNotificationQueryRepository.PolicyNotificationSummary;
import com.twojz.y_kit.policy.repository.PolicyNotificationRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyNotificationFindService {
    private final PolicyNotificationRepository policyNotificationRepository;

    public boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user) {
        return policyNotificationRepository.existsByPolicyAndUser(policy, user);
    }

    public Optional<PolicyNotificationEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user) {
        return policyNotificationRepository.findByPolicyAndUser(policy, user);
    }

    public List<PolicyNotificationResponse> findMyNotificationResponses(UserEntity user) {
        return policyNotificationRepository.findNotificationSummariesByUser(user).stream()
                .map(PolicyNotificationResponse::from)
                .toList();
    }

    public List<PendingPolicyNotification> findPendingNotificationsByDeadline(LocalDate targetDate) {
        return policyNotificationRepository.findPendingNotificationsByDeadline(targetDate);
    }
}
