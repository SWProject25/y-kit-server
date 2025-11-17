package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseParticipantEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseParticipantRepository extends JpaRepository<GroupPurchaseParticipantEntity, Long> {
    Optional<GroupPurchaseParticipantEntity> findByGroupPurchaseAndUser(GroupPurchaseEntity gp, UserEntity user);
}
