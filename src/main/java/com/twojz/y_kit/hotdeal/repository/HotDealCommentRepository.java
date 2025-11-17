package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.community.domain.entity.CommunityCommentEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealCommentRepository extends JpaRepository<HotDealCommentEntity, Long> {
    long countByHotDeal(HotDealEntity hotDeal);

    List<HotDealCommentEntity> findByHotDealOrderByCreatedAtDesc(HotDealEntity hotDeal);
}
