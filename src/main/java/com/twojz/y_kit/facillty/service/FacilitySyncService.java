package com.twojz.y_kit.facillty.service;

import com.twojz.y_kit.external.public_resource.PublicResourceClient;
import com.twojz.y_kit.external.public_resource.dto.ResourceData;
import com.twojz.y_kit.facillty.domain.entity.FacilityCategory;
import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import com.twojz.y_kit.facillty.repository.FacilityRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilitySyncService {

    private final PublicResourceClient publicResourceClient;
    private final FacilityRepository facilityRepository;

    private static final int PAGE_SIZE = 1000;

    /**
     * 전체 카테고리를 비동기로 병렬 수집
     */
    public void fetchAllFacilitiesAsync() {
        Flux.fromIterable(Arrays.asList(FacilityCategory.values()))
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::fetchCategoryAsync)
                .sequential()
                .subscribe(
                        v -> {},
                        err -> log.error("전체 시설 동기화 실패", err),
                        () -> log.info("전체 시설 동기화 완료!")
                );
    }


    /**
     * 특정 카테고리를 비동기로 페이지 전체 수집
     */
    private Flux<FacilityEntity> fetchCategoryAsync(FacilityCategory category) {
        log.info("📌 카테고리 처리 시작: {}", category);

        return fetchPageAsync(category, 1)
                .expand(tuple -> {
                    boolean hasNext = tuple.getT2();
                    int page = tuple.getT1();

                    if (hasNext) {
                        return fetchPageAsync(category, page + 1);
                    }
                    return Mono.empty();
                })
                .map(Tuple3::getT3)         // List<FacilityEntity>
                .flatMap(Flux::fromIterable)
                .flatMap(this::saveAsync);
    }


    /**
     * 한 페이지 비동기 호출
     * return → (pageNo, hasNextPage, convertedEntities)
     */
    private Mono<Tuple3<Integer, Boolean, List<FacilityEntity>>> fetchPageAsync(
            FacilityCategory category, int pageNo) {

        return publicResourceClient.fetchCategoryPage(category.getCode(), pageNo)
                .map(response -> {

                    List<ResourceData> data = response.getData();
                    if (data == null) data = List.of();

                    List<FacilityEntity> entities = data.stream()
                            .map(d -> convertToEntity(d, category))
                            .filter(e -> e.getAddress() != null && !e.getAddress().isEmpty())
                            .filter(e -> e.getLongitude() != null && e.getLatitude() != null)
                            .filter(e -> e.getLatitude() != 0 && e.getLongitude() != 0)
                            .toList();

                    boolean hasNext = data.size() == PAGE_SIZE;

                    return Tuples.of(pageNo, hasNext, entities);
                })
                .doOnNext(t -> log.info("📄 {} 페이지 완료 ({}개)", t.getT1(), t.getT3().size()));
    }


    /**
     * JPA 저장을 비동기로 실행
     */
    private Mono<FacilityEntity> saveAsync(FacilityEntity entity) {
        return Mono.fromCallable(() -> facilityRepository.save(entity))
                .subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * DTO → JPA Entity 변환
     */
    private FacilityEntity convertToEntity(ResourceData d, FacilityCategory category) {
        return FacilityEntity.builder()
                .resourceNo(d.getRsrcNo())
                .name(d.getRsrcNm())
                .zipCode(d.getZip())
                .address(d.getAddr())
                .detailAddress(d.getDaddr())
                .latitude(d.getLat())
                .longitude(d.getLot())
                .imgUrl(d.getImgFileUrlAddr())
                .reservationUrl(d.getInstUrlAddr())
                .category(category)
                .build();
    }
}