package com.twojz.y_kit.facillty.service;

import com.twojz.y_kit.external.public_resource.PublicResourceClient;
import com.twojz.y_kit.external.public_resource.dto.ResourceData;
import com.twojz.y_kit.facillty.domain.entity.FacilityCategory;
import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import com.twojz.y_kit.facillty.repository.FacilityRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
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
     * JPA 전용 Scheduler
     * - Hikari max-pool-size 이하로 맞추는 게 핵심
     */
    private static final Scheduler DB_SCHEDULER =
            Schedulers.newBoundedElastic(
                    8,      // 스레드 수 (Hikari pool <=)
                    100,
                    "jpa-scheduler"
            );

    /**
     * 매일 새벽 2시 동기화
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledSync() {
        log.info("시설 동기화 스케줄러 시작");
        fetchAllFacilitiesAsync();
    }

    /**
     * 전체 카테고리 동기화
     * - 카테고리 동시성 제한 (중요)
     */
    public void fetchAllFacilitiesAsync() {
        Flux.fromArray(FacilityCategory.values())
                .flatMap(this::fetchCategoryAsync, 2)
                .subscribe(
                        v -> {},
                        e -> log.error("전체 시설 동기화 실패", e),
                        () -> log.info("전체 시설 동기화 완료")
                );
    }

    /**
     * 카테고리 단위 동기화
     */
    private Mono<Void> fetchCategoryAsync(FacilityCategory category) {
        log.info("카테고리 동기화 시작: {}", category.getName());

        Set<String> syncedResourceNos = Collections.synchronizedSet(new HashSet<>());

        return fetchPageAsync(category, 1)
                .expand(tuple -> {
                    if (tuple.getT2()) {
                        return fetchPageAsync(category, tuple.getT1() + 1);
                    }
                    return Mono.empty();
                })
                .map(Tuple3::getT3)
                .flatMap(Flux::fromIterable)
                .flatMap(entity -> {
                    syncedResourceNos.add(entity.getResourceNo());
                    return saveOrUpdateAsync(entity);
                }, 5) // ⭐ DB 동시성 제한 (매우 중요)
                .then(
                        Mono.fromRunnable(() ->
                                deleteRemovedFacilities(category, syncedResourceNos)
                        ).subscribeOn(DB_SCHEDULER)
                )
                .doOnSuccess(v ->
                        log.info("카테고리 동기화 완료: {}", category.getName())
                ).then();
    }

    /**
     * 페이지 단위 API 호출
     */
    private Mono<Tuple3<Integer, Boolean, List<FacilityEntity>>> fetchPageAsync(
            FacilityCategory category, int pageNo) {

        return publicResourceClient.fetchCategoryPage(category.getCode(), pageNo)
                .map(response -> {
                    List<ResourceData> data =
                            Optional.ofNullable(response.getData()).orElse(List.of());

                    List<FacilityEntity> entities = data.stream()
                            .map(d -> convertToEntity(d, category))
                            .filter(e -> e.getAddress() != null && !e.getAddress().isEmpty())
                            .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                            .filter(e -> e.getLatitude() != 0 && e.getLongitude() != 0)
                            .toList();

                    boolean hasNext = data.size() == PublicResourceClient.PAGE_SIZE;
                    return Tuples.of(pageNo, hasNext, entities);
                })
                .doOnNext(t ->
                        log.info("{} 페이지 완료 ({}건)", t.getT1(), t.getT3().size())
                );
    }

    /**
     * 생성 또는 수정 (JPA 전용 Scheduler)
     */
    private Mono<Void> saveOrUpdateAsync(FacilityEntity entity) {
        return Mono.fromRunnable(() -> {
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
                })
                .subscribeOn(DB_SCHEDULER).then();
    }

    /**
     * 외부 API에서 삭제된 시설 정리
     */
    @Transactional
    public void deleteRemovedFacilities(
            FacilityCategory category,
            Set<String> syncedResourceNos
    ) {
        List<FacilityEntity> existing =
                facilityRepository.findByCategory(category);

        List<FacilityEntity> toDelete = existing.stream()
                .filter(f -> !syncedResourceNos.contains(f.getResourceNo()))
                .toList();

        if (!toDelete.isEmpty()) {
            facilityRepository.deleteAll(toDelete);
            log.info(
                    "카테고리 {} - 삭제 {}건",
                    category.getName(),
                    toDelete.size()
            );
        }
    }

    /**
     * DTO → Entity 변환
     */
    private FacilityEntity convertToEntity(
            ResourceData d,
            FacilityCategory category
    ) {
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