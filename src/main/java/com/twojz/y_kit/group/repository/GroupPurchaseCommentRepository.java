package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseCommentEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseCommentRepository extends JpaRepository<GroupPurchaseCommentEntity, Long> {
    List<GroupPurchaseCommentEntity> findByGroupPurchaseOrderByCreatedAtDesc(GroupPurchaseEntity groupPurchase);

    @EntityGraph(attributePaths = {"groupPurchase", "user"})
    List<GroupPurchaseCommentEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    long countByGroupPurchase(GroupPurchaseEntity groupPurchase);

    @Query("SELECT c.groupPurchase.id, COUNT(c) FROM GroupPurchaseCommentEntity c WHERE c.groupPurchase.id IN :groupPurchaseIds GROUP BY c.groupPurchase.id")
    List<Object[]> countByGroupPurchaseIds(@Param("groupPurchaseIds") List<Long> groupPurchaseIds);

    @Query("DELETE FROM GroupPurchaseCommentEntity c WHERE c.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}