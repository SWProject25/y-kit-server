package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealRepository extends JpaRepository<HotDealEntity, Long> {
    @EntityGraph(attributePaths = {"user", "region"})
    Page<HotDealEntity> findByCategory(HotDealCategory category, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "region"})
    Page<HotDealEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "region"})
    Page<HotDealEntity> findByUser(UserEntity user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "region"})
    @Query("SELECT DISTINCT h FROM HotDealEntity h " +
            "WHERE (:category IS NULL OR h.category = :category) " +
            "AND (:dealType IS NULL OR h.dealType = :dealType) " +
            "AND (" +
            "(:keyword1 IS NULL) OR " +
            "(h.title LIKE %:keyword1% OR h.content LIKE %:keyword1% OR h.placeName LIKE %:keyword1%) OR " +
            "(:keyword2 IS NOT NULL AND (h.title LIKE %:keyword2% OR h.content LIKE %:keyword2% OR h.placeName LIKE %:keyword2%)) OR " +
            "(:keyword3 IS NOT NULL AND (h.title LIKE %:keyword3% OR h.content LIKE %:keyword3% OR h.placeName LIKE %:keyword3%)) OR " +
            "(:keyword4 IS NOT NULL AND (h.title LIKE %:keyword4% OR h.content LIKE %:keyword4% OR h.placeName LIKE %:keyword4%)) OR " +
            "(:keyword5 IS NOT NULL AND (h.title LIKE %:keyword5% OR h.content LIKE %:keyword5% OR h.placeName LIKE %:keyword5%))" +
            ")")
    Page<HotDealEntity> searchByKeywords(
            @Param("category") HotDealCategory category,
            @Param("dealType") DealType dealType,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            @Param("keyword4") String keyword4,
            @Param("keyword5") String keyword5,
            Pageable pageable
    );
}