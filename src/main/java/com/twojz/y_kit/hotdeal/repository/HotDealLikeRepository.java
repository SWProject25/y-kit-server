package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotDealLikeRepository extends JpaRepository<HotDealLikeEntity, Long> {

    Optional<HotDealLikeEntity> findByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    boolean existsByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    long countByHotDeal(HotDealEntity hotDeal);

    @Query("SELECT h.id, COUNT(l) FROM HotDealLikeEntity l " +
            "JOIN l.hotDeal h WHERE h.id IN :hotDealIds GROUP BY h.id")
    List<Object[]> countByHotDealIds(@Param("hotDealIds") List<Long> hotDealIds);

    @Query("""
    SELECT l.hotDeal.id as hotDealId, COUNT(l) as count
    FROM HotDealLikeEntity l
    WHERE l.hotDeal.id IN :hotDealIds
    GROUP BY l.hotDeal.id
    """)
    List<LikeCountProjection> countByHotDealIdIn(@Param("hotDealIds") List<Long> hotDealIds);

    interface LikeCountProjection {
        Long getHotDealId();
        Long getCount();
    }
}