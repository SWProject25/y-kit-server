package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;

public interface PolicyBookmarkQueryRepository {
    List<Long> findBookmarkedPolicyIdsByUserAndPolicyIds(UserEntity user, List<Long> policyIds);
}
