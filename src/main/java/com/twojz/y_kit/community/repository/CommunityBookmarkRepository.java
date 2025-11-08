package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityBookmarkEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBookmarkRepository extends JpaRepository<CommunityBookmarkEntity, Long> {
    Optional<CommunityBookmarkEntity> findByCommunityAndUser(CommunityEntity community, UserEntity user);

    boolean existsByCommunityAndUser(CommunityEntity community, UserEntity user);

    List<CommunityBookmarkEntity> findByUser(UserEntity user);
}
