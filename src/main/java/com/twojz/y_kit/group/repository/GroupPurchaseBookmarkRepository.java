package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseBookmarkEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseBookmarkRepository extends JpaRepository<GroupPurchaseBookmarkEntity, Long> {
    Optional<GroupPurchaseBookmarkEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    @Modifying
    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    boolean existsByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    List<GroupPurchaseBookmarkEntity> findByUser(UserEntity user);

    @Query("SELECT gpb.groupPurchase.id FROM GroupPurchaseBookmarkEntity gpb " +
            "WHERE gpb.user = :user AND gpb.groupPurchase.id IN :groupPurchaseIds")
    List<Long> findBookmarkedGroupPurchaseIdsByUserAndGroupPurchaseIds(
            @Param("user") UserEntity user,
            @Param("groupPurchaseIds") List<Long> groupPurchaseIds
    );

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}