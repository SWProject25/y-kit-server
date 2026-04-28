package com.twojz.y_kit.policy.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twojz.y_kit.policy.domain.entity.QPolicyBookmarkEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PolicyBookmarkQueryRepositoryImpl implements PolicyBookmarkQueryRepository {

    private static final QPolicyBookmarkEntity bookmark = QPolicyBookmarkEntity.policyBookmarkEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findBookmarkedPolicyIdsByUserAndPolicyIds(UserEntity user, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(bookmark.policy.id)
                .from(bookmark)
                .where(
                        bookmark.user.eq(user),
                        bookmark.policy.id.in(policyIds)
                )
                .fetch();
    }
}
