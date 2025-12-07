package com.twojz.y_kit.region.repository;

import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.entity.RegionLevel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, String> {
    Optional<Region> findByCode(String code);

    Optional<Region> findByName(String name);

    Optional<Region> findByFullName(String fullName);

    List<Region> findAllByCodeIn(Collection<String> codes);

    List<Region> findByLevel(RegionLevel level);

    List<Region> findByParent_CodeAndLevel(String parentCode, RegionLevel level);

    List<Region> findAllByNameAndLevel(String name, RegionLevel level);

    Optional<Region> findByNameAndLevel(String name, RegionLevel level);
}
