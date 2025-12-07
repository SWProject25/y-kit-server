package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {
    Optional<BadgeEntity> findByName(String name);
}
