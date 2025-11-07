package com.twojz.y_kit.region.repository;

import com.twojz.y_kit.region.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, String> {
    Optional<Region> findByCode(String code);
    Optional<Region> findByName(String name);
}
