package com.twojz.y_kit.policy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twojz.y_kit.policy.domain.entity.QPolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyNotificationEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PolicyNotificationQueryRepositoryImpl implements PolicyNotificationQueryRepository {

    private static final QPolicyNotificationEntity notification = QPolicyNotificationEntity.policyNotificationEntity;
    private static final QPolicyEntity policy = QPolicyEntity.policyEntity;
    private static final QPolicyDetailEntity detail = QPolicyDetailEntity.policyDetailEntity;
    private static final QPolicyApplicationEntity application = QPolicyApplicationEntity.policyApplicationEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PolicyNotificationSummary> findNotificationSummariesByUser(UserEntity user) {
        return queryFactory
                .select(Projections.constructor(
                        PolicyNotificationSummary.class,
                        notification.policy.id,
                        detail.plcyNm,
                        detail.plcyExplnCn,
                        application.aplyEndYmd,
                        notification.createdAt,
                        notification.notificationSent
                ))
                .from(notification)
                .join(notification.policy, policy)
                .leftJoin(detail).on(detail.policy.eq(policy))
                .leftJoin(application).on(application.policy.eq(policy))
                .where(notification.user.eq(user))
                .orderBy(notification.createdAt.desc())
                .fetch();
    }

    @Override
    public List<PendingPolicyNotification> findPendingNotificationsByDeadline(LocalDate targetDate) {
        return queryFactory
                .select(Projections.constructor(
                        PendingPolicyNotification.class,
                        notification,
                        detail.plcyNm
                ))
                .from(notification)
                .join(notification.policy, policy)
                .join(application).on(application.policy.eq(policy))
                .leftJoin(detail).on(detail.policy.eq(policy))
                .where(
                        application.aplyEndYmd.eq(targetDate),
                        notification.notificationSent.isFalse()
                )
                .fetch();
    }
}
