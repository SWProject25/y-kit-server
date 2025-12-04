package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseCommentEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseCommentRepository extends JpaRepository<GroupPurchaseCommentEntity, Long> {
    List<GroupPurchaseCommentEntity> findByGroupPurchaseOrderByCreatedAtDesc(GroupPurchaseEntity groupPurchase);

    void deleteByGroupPurchase(GroupPurchaseEntity groupPurchase);

    long countByGroupPurchase(GroupPurchaseEntity groupPurchase);
}