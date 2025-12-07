package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityEntity, Long> {
    @EntityGraph(attributePaths = "user")
    Page<CommunityEntity> findByCategory(CommunityCategory category, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<CommunityEntity> findAll(Pageable pageable);

    Page<CommunityEntity> findByUser(UserEntity user, Pageable pageable);

    long countByUser(UserEntity user);

    @EntityGraph(attributePaths = "user")
    @Query("SELECT DISTINCT c FROM CommunityEntity c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (" +
            "(:keyword1 IS NOT NULL AND (c.title LIKE %:keyword1% OR c.content LIKE %:keyword1%)) OR " +
            "(:keyword2 IS NOT NULL AND (c.title LIKE %:keyword2% OR c.content LIKE %:keyword2%)) OR " +
            "(:keyword3 IS NOT NULL AND (c.title LIKE %:keyword3% OR c.content LIKE %:keyword3%)) OR " +
            "(:keyword4 IS NOT NULL AND (c.title LIKE %:keyword4% OR c.content LIKE %:keyword4%)) OR " +
            "(:keyword5 IS NOT NULL AND (c.title LIKE %:keyword5% OR c.content LIKE %:keyword5%)) OR " +
            "(:keyword1 IS NULL AND :keyword2 IS NULL AND :keyword3 IS NULL AND :keyword4 IS NULL AND :keyword5 IS NULL)" +
            ")")
    Page<CommunityEntity> searchByKeywords(
            @Param("category") CommunityCategory category,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            @Param("keyword4") String keyword4,
            @Param("keyword5") String keyword5,
            Pageable pageable
    );

    // 실시간 순위 조회 (조회수 + 북마크 수 기준으로 정렬)
    @EntityGraph(attributePaths = "user")
    @Query("SELECT c FROM CommunityEntity c " +
            "LEFT JOIN CommunityBookmarkEntity b ON b.community = c " +
            "GROUP BY c " +
            "ORDER BY (c.viewCount + COUNT(b)) DESC")
    List<CommunityEntity> findTrendingCommunities(Pageable pageable);

    @Query("SELECT c FROM CommunityEntity c JOIN FETCH c.user ORDER BY FUNCTION('RAND')")
    List<CommunityEntity> findRandomCommunities(@Param("limit") int limit);

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}
