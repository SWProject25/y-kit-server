package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyNotificationRepository extends JpaRepository<PolicyNotificationEntity, Long>,
        PolicyNotificationQueryRepository {
    boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user);

    Optional<PolicyNotificationEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user);

    void deleteByPolicyAndUser(PolicyEntity policy, UserEntity user);

    void deleteByUser(UserEntity user);
}
