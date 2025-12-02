package com.twojz.y_kit.group.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.group.dto.response.GroupPurchaseCommentResponse;
import com.twojz.y_kit.group.dto.response.GroupPurchaseDetailResponse;
import com.twojz.y_kit.group.dto.response.GroupPurchaseListResponse;
import com.twojz.y_kit.group.repository.GroupPurchaseCommentRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseParticipantRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupPurchaseFindService {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final GroupPurchaseParticipantRepository participantRepository;
    private final GroupPurchaseCommentRepository commentRepository;

    public GroupPurchaseEntity findById(Long groupPurchaseId) {
        return groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매를 찾을 수 없습니다."));
    }

    public GroupPurchaseDetailResponse getGroupPurchaseDetail(Long gpId, Long userId) {
        GroupPurchaseWithCountsDto dto = groupPurchaseRepository
                .findGroupPurchaseWithCountsById(gpId, userId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매를 찾을 수 없습니다."));

        GroupPurchaseEntity gp = findById(gpId);
        String authorName = gp.getUser().getName();

        List<GroupPurchaseCommentResponse> comments = commentRepository
                .findByGroupPurchaseOrderByCreatedAtDesc(gp)
                .stream()
                .map(GroupPurchaseCommentResponse::from)
                .toList();

        boolean isParticipating = participantRepository
                .existsByUserIdAndGroupPurchaseId(userId, gpId);

        return GroupPurchaseDetailResponse.fromDto(
                dto,
                comments,
                authorName,
                isParticipating
        );
    }

    public PageResponse<GroupPurchaseListResponse> getGroupPurchaseList(Long userId, Pageable pageable) {
        Page<GroupPurchaseWithCountsDto> dtos = groupPurchaseRepository
                .findGroupPurchasesWithCounts(userId, pageable);
        return new PageResponse<>(dtos.map(GroupPurchaseListResponse::fromDto));
    }

    public PageResponse<GroupPurchaseListResponse> searchGroupPurchases(
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        Page<GroupPurchaseWithCountsDto> dtos = groupPurchaseRepository
                .findGroupPurchasesWithCountsByKeyword(keyword, userId, pageable);
        return new PageResponse<>(dtos.map(GroupPurchaseListResponse::fromDto));
    }

    public PageResponse<GroupPurchaseListResponse> getGroupPurchasesByStatus(
            GroupPurchaseStatus status,
            Long userId,
            Pageable pageable
    ) {
        Page<GroupPurchaseWithCountsDto> dtos = groupPurchaseRepository
                .findGroupPurchasesWithCountsByStatus(status, userId, pageable);
        return new PageResponse<>(dtos.map(GroupPurchaseListResponse::fromDto));
    }

    public PageResponse<GroupPurchaseListResponse> getGroupPurchasesByRegion(
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        Page<GroupPurchaseWithCountsDto> dtos = groupPurchaseRepository
                .findGroupPurchasesWithCountsByRegionCode(regionCode, userId, pageable);
        return new PageResponse<>(dtos.map(GroupPurchaseListResponse::fromDto));
    }

    public PageResponse<GroupPurchaseListResponse> getMyGroupPurchases(Long userId, Pageable pageable) {
        Page<GroupPurchaseWithCountsDto> dtos = groupPurchaseRepository
                .findGroupPurchasesWithCountsByUserId(userId, pageable);
        return new PageResponse<>(dtos.map(GroupPurchaseListResponse::fromDto));
    }

    public PageResponse<GroupPurchaseListResponse> getGroupPurchasesByStatusAndRegion(
            GroupPurchaseStatus status,
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        Page<GroupPurchaseWithCountsDto> page = groupPurchaseRepository
                .findGroupPurchasesWithCountsByStatusAndRegionCode(status, regionCode, userId, pageable);

        return new PageResponse<>(page.map(GroupPurchaseListResponse::fromDto));
    }
}