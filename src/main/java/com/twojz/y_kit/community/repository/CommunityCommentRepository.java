package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityCommentEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityCommentEntity, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<CommunityCommentEntity> findByCommunityOrderByCreatedAtDesc(CommunityEntity community);

    @EntityGraph(attributePaths = {"community", "user"})
    List<CommunityCommentEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    long countByCommunity(CommunityEntity community);

    @Query("SELECT c.community.id, COUNT(c) FROM CommunityCommentEntity c WHERE c.community.id IN :communityIds GROUP BY c.community.id")
    List<Object[]> countByCommunityIds(@Param("communityIds") List<Long> communityIds);

    @Query("DELETE FROM CommunityCommentEntity c WHERE c.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
