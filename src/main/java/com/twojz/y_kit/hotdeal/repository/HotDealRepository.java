package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealRepository extends JpaRepository<HotDealEntity, Long>, JpaSpecificationExecutor<HotDealEntity> {
    Page<HotDealEntity> findByTitleContaining(String keyword, Pageable pageable);

    Page<HotDealEntity> findByUser(UserEntity user, Pageable pageable);

    @Query("""
    SELECT h FROM HotDealEntity h
    JOIN FETCH h.region
    LEFT JOIN FETCH h.user
    """)
    List<HotDealEntity> findAllWithFetchJoin();

}
