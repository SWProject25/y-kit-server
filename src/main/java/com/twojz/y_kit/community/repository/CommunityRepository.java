package com.twojz.y_kit.community.repository;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityEntity, Long> {
    Page<CommunityEntity> findByCategory(CommunityCategory category, Pageable pageable);

    Page<CommunityEntity> findAll(Pageable pageable);

    Page<CommunityEntity> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    Page<CommunityEntity> findByUser(UserEntity user, Pageable pageable);
}
