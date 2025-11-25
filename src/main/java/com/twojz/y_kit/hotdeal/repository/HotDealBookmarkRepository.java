package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealBookmarkEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotDealBookmarkRepository extends JpaRepository<HotDealBookmarkEntity, Long> {

    Optional<HotDealBookmarkEntity> findByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    boolean existsByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    List<HotDealBookmarkEntity> findByUser(UserEntity user);
}