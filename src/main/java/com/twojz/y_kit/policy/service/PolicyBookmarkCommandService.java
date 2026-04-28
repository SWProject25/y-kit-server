package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyBookmarkCommandService {
    private final PolicyBookmarkRepository policyBookmarkRepository;

    public PolicyBookmarkEntity save(PolicyBookmarkEntity bookmark) {
        return policyBookmarkRepository.save(bookmark);
    }

    public void delete(PolicyBookmarkEntity bookmark) {
        policyBookmarkRepository.delete(bookmark);
    }

    public void deleteByUser(UserEntity user) {
        policyBookmarkRepository.deleteByUser(user);
    }
}
