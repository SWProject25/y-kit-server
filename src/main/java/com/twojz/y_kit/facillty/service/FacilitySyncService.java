package com.twojz.y_kit.facillty.service;

import com.twojz.y_kit.external.public_resource.PublicResourceClient;
import com.twojz.y_kit.external.public_resource.dto.ResourceData;
import com.twojz.y_kit.facillty.domain.entity.FacilityCategory;
import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import com.twojz.y_kit.facillty.repository.FacilityRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 매일 밤 12시에 자동 동기화 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledSync() {
        log.info("스케줄러 시작: 매일 자정 시설 동기화");
        fetchAllFacilitiesAsync();
    }

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
                        () -> {
                            log.info("전체 시설 동기화 완료!");
                        }
                );
    }

    /**
     * 특정 카테고리를 비동기로 페이지 전체 수집
     */
    private Mono<Void> fetchCategoryAsync(FacilityCategory category) {
        log.info("카테고리 동기화 시작: {}", category.getName());

        Set<String> syncedResourceNos = new HashSet<>();

        return fetchPageAsync(category, 1)
                .expand(tuple -> {
                    boolean hasNext = tuple.getT2();
                    int page = tuple.getT1();
                    if (hasNext) {
                        return fetchPageAsync(category, page + 1);
                    }
                    return Mono.empty();
                })
                .map(Tuple3::getT3)
                .flatMap(Flux::fromIterable)
                .flatMap(entity -> {
                    syncedResourceNos.add(entity.getResourceNo());
                    return saveOrUpdateAsync(entity);
                }, 20)
                .then()
                .doOnSuccess(v -> {
                    log.info("카테고리 동기화 완료: {}", category.getName());
                    deleteRemovedFacilities(category, syncedResourceNos);
                });
    }

    /**
     * 한 페이지 비동기 호출
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

                    boolean hasNext = data.size() == PublicResourceClient.PAGE_SIZE;

                    return Tuples.of(pageNo, hasNext, entities);
                })
                .doOnNext(t -> log.info("{} 페이지 완료 ({}개)", t.getT1(), t.getT3().size()));
    }

    /**
     * 생성 또는 수정 (비동기)
     */
    private Mono<Void> saveOrUpdateAsync(FacilityEntity entity) {
        return Mono.fromCallable(() -> {
                    facilityRepository.findByResourceNo(entity.getResourceNo())
                            .ifPresentOrElse(
                                    existing -> {
                                        existing.update(entity);
                                        facilityRepository.save(existing);
                                        log.debug("시설 수정: {}", entity.getResourceNo());
                                    },
                                    () -> {
                                        facilityRepository.save(entity);
                                        log.debug("시설 생성: {}", entity.getResourceNo());
                                    }
                            );
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * 특정 카테고리에서 외부 API에서 삭제된 시설 제거
     */
    @Transactional
    public void deleteRemovedFacilities(FacilityCategory category, Set<String> syncedResourceNos) {
        List<FacilityEntity> existingFacilities =
                facilityRepository.findByCategory(category);

        List<FacilityEntity> toDelete = existingFacilities.stream()
                .filter(f -> !syncedResourceNos.contains(f.getResourceNo()))
                .toList();

        if (!toDelete.isEmpty()) {
            facilityRepository.deleteAll(toDelete);
            log.info("카테고리 {} - 삭제된 시설: {}개", category.getName(), toDelete.size());
        }
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