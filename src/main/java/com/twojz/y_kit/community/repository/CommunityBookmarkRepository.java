package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityBookmarkEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBookmarkRepository extends JpaRepository<CommunityBookmarkEntity, Long> {
    Optional<CommunityBookmarkEntity> findByCommunityAndUser(CommunityEntity community, UserEntity user);

    boolean existsByCommunityAndUser(CommunityEntity community, UserEntity user);

    @Modifying
    void deleteByCommunity(CommunityEntity community);

    List<CommunityBookmarkEntity> findByUser(UserEntity user);

    long countByCommunity(CommunityEntity community);

    @Query("SELECT cb.community.id FROM CommunityBookmarkEntity cb " +
            "WHERE cb.user = :user AND cb.community.id IN :communityIds")
    List<Long> findBookmarkedCommunityIdsByUserAndCommunityIds(
            @Param("user") UserEntity user,
            @Param("communityIds") List<Long> communityIds
    );

    @Modifying
    void deleteByUser(@Param("user") UserEntity user);
}
