package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findBySocialIdAndLoginProvider(String socialId, LoginProvider loginProvider);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByNickName(String nickName);

    @Query("""
            SELECT DISTINCT u FROM UserEntity u
            JOIN UserDeviceEntity d ON d.user.id = u.id
            WHERE u.createdAt >= :sevenDaysAgo
            AND u.profileStatus != 'COMPLETED'
            AND d.isActive = true
            AND d.notificationEnabled = true
            """)
    List<UserEntity> findUsersForProfileReminder(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

}