package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseLikeRepository extends JpaRepository<GroupPurchaseLikeEntity, Long> {
    Optional<GroupPurchaseLikeEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    boolean existsByGroupPurchaseAndUser(GroupPurchaseEntity groupPurchase, UserEntity user);

    long countByGroupPurchase(GroupPurchaseEntity groupPurchase);

    List<GroupPurchaseLikeEntity> findByUser(UserEntity user);
}

