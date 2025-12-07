package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.entity.CommunityLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityLikeRepository extends JpaRepository<CommunityLikeEntity, Long> {
    Optional<CommunityLikeEntity> findByCommunityAndUser(CommunityEntity community, UserEntity user);

    long countByCommunity(CommunityEntity community);

    boolean existsByCommunityAndUser(CommunityEntity community, UserEntity user);

    @Modifying
    void deleteByCommunity(CommunityEntity community);

    @Query("SELECT l.community.id, COUNT(l) FROM CommunityLikeEntity l WHERE l.community.id IN :communityIds GROUP BY l.community.id")
    List<Object[]> countByCommunityIds(@Param("communityIds") List<Long> communityIds);

    @Query("SELECT l.community FROM CommunityLikeEntity l WHERE l.user = :user")
    List<CommunityEntity> findCommunitiesByUser(@Param("user") UserEntity user);

    List<CommunityLikeEntity> findByUser(UserEntity user);

    @Query("SELECT cl.community.id FROM CommunityLikeEntity cl " +
            "WHERE cl.user = :user AND cl.community.id IN :communityIds")
    List<Long> findLikedCommunityIdsByUserAndCommunityIds(
            @Param("user") UserEntity user,
            @Param("communityIds") List<Long> communityIds
    );

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}
