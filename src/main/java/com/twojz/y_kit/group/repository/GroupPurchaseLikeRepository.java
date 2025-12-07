package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseLikeRepository extends JpaRepository<GroupPurchaseLikeEntity, Long> {
    Optional<GroupPurchaseLikeEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    @Modifying
    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    boolean existsByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    long countByGroupPurchase(GroupPurchaseEntity groupPurchase);

    List<GroupPurchaseLikeEntity> findByUser(UserEntity user);

    @Query("SELECT l.groupPurchase.id, COUNT(l) FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase.id IN :groupPurchaseIds GROUP BY l.groupPurchase.id")
    List<Object[]> countByGroupPurchaseIds(@Param("groupPurchaseIds") List<Long> groupPurchaseIds);

    @Query("SELECT gpl.groupPurchase.id FROM GroupPurchaseLikeEntity gpl " +
            "WHERE gpl.user = :user AND gpl.groupPurchase.id IN :groupPurchaseIds")
    List<Long> findLikedGroupPurchaseIdsByUserAndGroupPurchaseIds(
            @Param("user") UserEntity user,
            @Param("groupPurchaseIds") List<Long> groupPurchaseIds
    );

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}

