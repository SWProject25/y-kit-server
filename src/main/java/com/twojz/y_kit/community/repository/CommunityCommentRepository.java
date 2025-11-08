package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityCommentEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityCommentEntity, Long> {
    List<CommunityCommentEntity> findByCommunityOrderByCreatedAtDesc(CommunityEntity community);

    long countByCommunity(CommunityEntity community);
}
