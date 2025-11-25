package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotDealCommentFindService {
    private final HotDealCommentRepository commentRepository;

    public HotDealCommentEntity findComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    public List<HotDealCommentEntity> findComments(HotDealEntity hotDeal) {
        return commentRepository.findByHotDealOrderByCreatedAtDesc(hotDeal);
    }
}
