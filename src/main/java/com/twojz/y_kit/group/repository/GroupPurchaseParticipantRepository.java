package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseParticipantEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseParticipantRepository extends JpaRepository<GroupPurchaseParticipantEntity, Long> {
    Optional<GroupPurchaseParticipantEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    @Modifying
    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    boolean existsByUserIdAndGroupPurchaseId(Long userId, Long groupPurchaseId);

    List<GroupPurchaseParticipantEntity> findByUser(UserEntity user);

    @Query("SELECT gpp.groupPurchase.id FROM GroupPurchaseParticipantEntity gpp " +
            "WHERE gpp.user.id = :userId AND gpp.groupPurchase.id IN :groupPurchaseIds")
    List<Long> findParticipatingGroupPurchaseIdsByUserIdAndGroupPurchaseIds(
            @Param("userId") Long userId,
            @Param("groupPurchaseIds") List<Long> groupPurchaseIds
    );

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}