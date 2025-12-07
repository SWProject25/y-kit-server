package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealBookmarkEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotDealBookmarkRepository extends JpaRepository<HotDealBookmarkEntity, Long> {

    Optional<HotDealBookmarkEntity> findByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    boolean existsByHotDealAndUser(HotDealEntity hotDeal, UserEntity user);

    List<HotDealBookmarkEntity> findByUser(UserEntity user);

    @Query("SELECT b.hotDeal.id FROM HotDealBookmarkEntity b WHERE b.user = :user AND b.hotDeal.id IN :hotDealIds")
    List<Long> findBookmarkedHotDealIdsByUserAndHotDealIds(@Param("user") UserEntity user, @Param("hotDealIds") List<Long> hotDealIds);

    // 사용자의 모든 핫딜 북마크 삭제
    @Modifying
    @Query("DELETE FROM HotDealBookmarkEntity b WHERE b.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}