package com.twojz.y_kit.facillty.service;

import com.twojz.y_kit.facillty.dto.response.FacilityMapResponse;
import com.twojz.y_kit.facillty.repository.FacilityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacilityFindService {
    private final FacilityRepository facilityRepository;

    public List<FacilityMapResponse> findFacilitiesInBounds(
            double minLat, double maxLat, double minLng, double maxLng) {

        return facilityRepository
                .findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(FacilityMapResponse::from)
                .toList();
    }
}