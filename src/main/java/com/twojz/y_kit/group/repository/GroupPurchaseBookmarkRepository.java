package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseBookmarkEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseBookmarkRepository extends JpaRepository<GroupPurchaseBookmarkEntity, Long> {
    Optional<GroupPurchaseBookmarkEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity gp, UserEntity user);
}
