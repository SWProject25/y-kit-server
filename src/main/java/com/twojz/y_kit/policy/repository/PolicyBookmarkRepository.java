package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyBookmarkRepository extends JpaRepository<PolicyBookmarkEntity, Long>,
        PolicyBookmarkQueryRepository {
    boolean existsByPolicyAndUser(PolicyEntity policy, UserEntity user);

    Optional<PolicyBookmarkEntity> findByPolicyAndUser(PolicyEntity policy, UserEntity user);

    List<PolicyBookmarkEntity> findByUser(UserEntity user);

    @Query("SELECT b FROM PolicyBookmarkEntity b " +
            "JOIN FETCH b.policy p " +
            "JOIN PolicyDetailEntity d ON d.policy = p " +
            "WHERE b.user = :user " +
            "ORDER BY b.createdAt DESC")
    List<PolicyBookmarkEntity> findByUserWithDetailOrderByCreatedAtDesc(@Param("user") UserEntity user);

    // 사용자의 모든 정책 북마크 삭제
    void deleteByUser(UserEntity user);
}
