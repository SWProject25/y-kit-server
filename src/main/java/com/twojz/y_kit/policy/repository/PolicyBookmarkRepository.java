package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyBookmarkRepository extends JpaRepository<PolicyBookmarkEntity, Long> {
    boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user);

    Optional<PolicyBookmarkEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user);

    List<PolicyBookmarkEntity> findByUser(UserEntity user);

    @Query("SELECT b.policy.id FROM PolicyBookmarkEntity b WHERE b.user = :user AND b.policy.id IN :policyIds")
    List<Long> findBookmarkedPolicyIdsByUserAndPolicyIds(@Param("user") UserEntity user, @Param("policyIds") List<Long> policyIds);

    List<PolicyBookmarkEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    // 사용자의 모든 정책 북마크 삭제
    @Query("DELETE FROM PolicyBookmarkEntity b WHERE b.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
