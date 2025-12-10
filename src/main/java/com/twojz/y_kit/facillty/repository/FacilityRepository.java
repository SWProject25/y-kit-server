package com.twojz.y_kit.facillty.repository;

import com.twojz.y_kit.facillty.domain.entity.FacilityCategory;
import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<FacilityEntity, Long> {
    Optional<FacilityEntity> findByResourceNo(String resourceNo);

    List<FacilityEntity> findByCategory(FacilityCategory category);

    List<FacilityEntity> findByUpdatedAtBefore(LocalDateTime dateTime);

    boolean existsByResourceNo(String resourceNo);

    List<FacilityEntity> findByLatitudeBetweenAndLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng
    );
}
