package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyNotificationRepository extends JpaRepository<PolicyNotificationEntity, Long> {
    boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user);

    Optional<PolicyNotificationEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user);

    void deleteByPolicyAndUser(PolicyEntity policy, UserEntity user);

    // 사용자가 신청한 정책 알림 목록 조회 (최신순)
    @Query("SELECT pn FROM PolicyNotificationEntity pn " +
            "JOIN FETCH pn.policy p " +
            "JOIN FETCH p.detail " +
            "WHERE pn.user = :user " +
            "ORDER BY pn.createdAt DESC")
    List<PolicyNotificationEntity> findByUserOrderByCreatedAtDesc(@Param("user") UserEntity user);

    // 마감 일주일 전인 정책들 중 알림 미발송된 것 조회
    @Query("SELECT pn FROM PolicyNotificationEntity pn " +
            "JOIN FETCH pn.policy p " +
            "JOIN FETCH pn.user u " +
            "JOIN FETCH p.application pa " +
            "WHERE pa.aplyEndYmd = :targetDate " +
            "AND pn.notificationSent = false")
    List<PolicyNotificationEntity> findPendingNotificationsByDeadline(@Param("targetDate") LocalDate targetDate);

    @Query("DELETE FROM PolicyNotificationEntity n WHERE n.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
