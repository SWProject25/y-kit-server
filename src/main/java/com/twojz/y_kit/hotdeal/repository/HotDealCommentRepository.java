package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealCommentRepository extends JpaRepository<HotDealCommentEntity, Long> {
    long countByHotDeal(HotDealEntity hotDeal);

    List<HotDealCommentEntity> findByHotDealOrderByCreatedAtDesc(HotDealEntity hotDeal);

    @EntityGraph(attributePaths = {"hotDeal", "user"})
    List<HotDealCommentEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

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

    @Modifying
    @Query("DELETE FROM HotDealCommentEntity c WHERE c.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
