package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotDealLikeRepository extends JpaRepository<HotDealLikeEntity, Long> {

    Optional<HotDealLikeEntity> findByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    boolean existsByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    long countByHotDeal(HotDealEntity hotDeal);
}