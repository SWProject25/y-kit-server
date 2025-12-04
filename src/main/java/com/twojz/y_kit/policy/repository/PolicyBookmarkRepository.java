package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyBookmarkRepository extends JpaRepository<PolicyBookmarkEntity, Long> {
    Optional<PolicyBookmarkEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user);

    boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user);

    List<PolicyBookmarkEntity> findByUser(UserEntity user);

    List<PolicyBookmarkEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    void deleteByPolicy(PolicyEntity policy);

    long countByPolicy(PolicyEntity policy);
}
