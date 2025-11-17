package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.UserBadgeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {
    Optional<UserBadgeEntity> findByUserIdAndBadgeId(Long userId, Long badgeId);

    void deleteByUserIdAndBadgeId(Long userId, Long badgeId);

    @Query("SELECT ub FROM UserBadgeEntity ub " +
            "JOIN FETCH ub.badge " +
            "WHERE ub.user.id = :userId " +
            "ORDER BY ub.acquiredAt DESC")
    List<UserBadgeEntity> findAllByUserIdWithBadge(@Param("userId") Long userId);
}
