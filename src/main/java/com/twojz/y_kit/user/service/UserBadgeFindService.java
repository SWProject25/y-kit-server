package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.UserBadgeEntity;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBadgeFindService {
    private final UserBadgeRepository userBadgeRepository;

    public List<UserBadgeEntity> getUserBadges(Long userId) {
        return userBadgeRepository.findAllByUserId(userId);
    }
}
