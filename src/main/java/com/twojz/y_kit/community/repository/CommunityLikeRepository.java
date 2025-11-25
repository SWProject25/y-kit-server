package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.entity.CommunityLikeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityLikeRepository extends JpaRepository<CommunityLikeEntity, Long> {
    Optional<CommunityLikeEntity> findByCommunityAndUser(CommunityEntity community, UserEntity user);

    long countByCommunity(CommunityEntity community);

    boolean existsByCommunityAndUser(CommunityEntity community, UserEntity user);
}
