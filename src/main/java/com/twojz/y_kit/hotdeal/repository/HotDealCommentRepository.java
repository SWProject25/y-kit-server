package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealCommentRepository extends JpaRepository<HotDealCommentEntity, Long> {
    long countByHotDeal(HotDealEntity hotDeal);

    List<HotDealCommentEntity> findByHotDealOrderByCreatedAtDesc(HotDealEntity hotDeal);

    @Query("SELECT h.id, COUNT(c) FROM HotDealCommentEntity c " +
            "JOIN c.hotDeal h WHERE h.id IN :hotDealIds GROUP BY h.id")
    List<Object[]> countByHotDealIds(@Param("hotDealIds") List<Long> hotDealIds);

    @Query("""
    SELECT c.hotDeal.id as hotDealId, COUNT(c) as count
    FROM HotDealCommentEntity c
    WHERE c.hotDeal.id IN :hotDealIds
    GROUP BY c.hotDeal.id
    """)
    List<CommentCountProjection> countByHotDealIdIn(@Param("hotDealIds") List<Long> hotDealIds);

    interface CommentCountProjection {
        Long getHotDealId();
        Long getCount();
    }
}
