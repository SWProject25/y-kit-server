package com.twojz.y_kit.policy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.QPolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.QPolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.entity.QPolicyRegion;
import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PolicyQueryRepositoryImpl implements PolicyQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPolicyEntity p = QPolicyEntity.policyEntity;
    private static final QPolicyDetailEntity d = QPolicyDetailEntity.policyDetailEntity;
    private static final QPolicyApplicationEntity a = QPolicyApplicationEntity.policyApplicationEntity;
    private static final QPolicyQualificationEntity q = QPolicyQualificationEntity.policyQualificationEntity;
    private static final QPolicyCategoryMapping cm = QPolicyCategoryMapping.policyCategoryMapping;
    private static final QPolicyKeywordMapping km = QPolicyKeywordMapping.policyKeywordMapping;
    private static final QPolicyRegion pr = QPolicyRegion.policyRegion;

    @Override
    public Page<PolicyEntity> findAllActive(Pageable pageable) {
        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue())
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findByCategoryId(Long categoryId, Pageable pageable) {
        var inCategory = p.id.in(
                JPAExpressions.select(cm.policy.id)
                        .from(cm)
                        .where(cm.category.id.eq(categoryId))
        );
        var where = new BooleanBuilder(p.isActive.isTrue()).and(inCategory);

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findByKeywords(List<String> keywords, Pageable pageable) {
        var where = new BooleanBuilder(p.isActive.isTrue())
                .and(buildTextSearchCondition(keywords));

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findByRegionCode(String regionCode, Pageable pageable) {
        var inRegion = p.id.in(
                JPAExpressions.select(pr.policy.id)
                        .from(pr)
                        .where(pr.region.code.eq(regionCode))
        );
        var where = new BooleanBuilder(p.isActive.isTrue()).and(inRegion);

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findByAge(Integer age, Pageable pageable) {
        var where = new BooleanBuilder(p.isActive.isTrue())
                .and(buildAgeExclusionCondition(age).not());

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findApplicationAvailable(LocalDate today, Pageable pageable) {
        var available = p.id.in(
                JPAExpressions.select(a.policy.id)
                        .from(a)
                        .where(a.aplyBgngYmd.loe(today).and(a.aplyEndYmd.goe(today)))
        );
        var where = new BooleanBuilder(p.isActive.isTrue()).and(available);

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findByKeywordsAndAge(List<String> keywords, Integer age, Pageable pageable) {
        var where = new BooleanBuilder(p.isActive.isTrue())
                .and(buildTextSearchCondition(keywords))
                .and(buildAgeExclusionCondition(age).not());

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findRecommendedWithCategory(Integer age, String regionCode, Pageable pageable) {
        var where = new BooleanBuilder(p.isActive.isTrue())
                .and(buildRegionCondition(regionCode))
                .and(buildAgeExclusionCondition(age).not());

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .where(where)
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.count())
                .from(p)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findRecommendedWithProfile(Integer age, String regionCode,
            EmploymentStatus employmentStatus, EducationLevel educationLevel, MajorField major,
            Pageable pageable) {

        var where = new BooleanBuilder(p.isActive.isTrue())
                .and(buildRegionCondition(regionCode))
                .and(buildProfileExclusionCondition(age, employmentStatus, educationLevel, major).not());

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findPopularByViewCount(Pageable pageable) {
        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue())
                .distinct()
                .orderBy(p.viewCount.desc(), p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findPopularByBookmarkCount(Pageable pageable) {
        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue())
                .distinct()
                .orderBy(p.bookmarkCount.desc(), p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(p.isActive.isTrue());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PolicyEntity> findDeadlineSoon(LocalDate today, Pageable pageable) {
        // 마감일 기준 정렬이 필요하므로 PolicyApplicationEntity JOIN
        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .join(a).on(a.policy.id.eq(p.id))
                .where(p.isActive.isTrue()
                        .and(a.aplyBgngYmd.loe(today))
                        .and(a.aplyEndYmd.goe(today)))
                .distinct()
                .orderBy(a.aplyEndYmd.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .join(a).on(a.policy.id.eq(p.id))
                .where(p.isActive.isTrue()
                        .and(a.aplyBgngYmd.loe(today))
                        .and(a.aplyEndYmd.goe(today)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<PolicyEntity> findSimilarByCategory(Long policyId, int limit) {
        QPolicyCategoryMapping cm2 = new QPolicyCategoryMapping("cm2");

        List<Long> categoryIds = queryFactory
                .select(cm.category.id)
                .from(cm)
                .where(cm.policy.id.eq(policyId))
                .fetch();

        if (categoryIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .join(cm2).on(cm2.policy.id.eq(p.id))
                .where(p.isActive.isTrue()
                        .and(p.id.ne(policyId))
                        .and(cm2.category.id.in(categoryIds)))
                .orderBy(p.viewCount.desc(), p.createdAt.desc())
                .distinct()
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<PolicyEntity> searchPolicies(List<Long> categoryIds, List<Long> keywordIds,
            List<String> keywords, Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder(p.isActive.isTrue());

        if (categoryIds != null && !categoryIds.isEmpty()) {
            where.and(p.id.in(
                    JPAExpressions.select(cm.policy.id)
                            .from(cm)
                            .where(cm.category.id.in(categoryIds))
            ));
        }

        if (keywordIds != null && !keywordIds.isEmpty()) {
            where.and(p.id.in(
                    JPAExpressions.select(km.policy.id)
                            .from(km)
                            .where(km.keyword.id.in(keywordIds))
            ));
        }

        if (keywords != null && !keywords.isEmpty()) {
            BooleanBuilder textOr = new BooleanBuilder();
            for (String kw : keywords) {
                textOr.or(p.id.in(
                        JPAExpressions.select(d.policy.id)
                                .from(d)
                                .where(d.plcyNm.containsIgnoreCase(kw)
                                        .or(d.plcyExplnCn.containsIgnoreCase(kw)))
                ));
            }
            where.and(textOr);
        }

        List<PolicyEntity> content = queryFactory
                .selectFrom(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where)
                .distinct()
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.countDistinct())
                .from(p)
                .join(d).on(d.policy.id.eq(p.id))
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // ===================== 헬퍼 메서드 =====================

    /** 텍스트 키워드 OR 검색 (정책명 + 설명) */
    private BooleanBuilder buildTextSearchCondition(List<String> keywords) {
        BooleanBuilder orBuilder = new BooleanBuilder();
        if (keywords == null || keywords.isEmpty()) return orBuilder;

        for (String kw : keywords) {
            orBuilder.or(p.id.in(
                    JPAExpressions.select(d.policy.id)
                            .from(d)
                            .where(d.plcyNm.containsIgnoreCase(kw)
                                    .or(d.plcyExplnCn.containsIgnoreCase(kw)))
            ));
            orBuilder.or(p.id.in(
                    JPAExpressions.select(km.policy.id)
                            .from(km)
                            .where(km.keyword.keyword.containsIgnoreCase(kw))
            ));
        }
        return orBuilder;
    }

    /** 연령 조건을 통과하지 못하는 정책 ID를 IN 서브쿼리로 반환 */
    private com.querydsl.core.types.dsl.BooleanExpression buildAgeExclusionCondition(Integer age) {
        return p.id.in(
                JPAExpressions.select(q.policy.id)
                        .from(q)
                        .where(
                                q.sprtTrgtMinAge.isNotNull().and(q.sprtTrgtMinAge.gt(age))
                                        .or(q.sprtTrgtMaxAge.isNotNull().and(q.sprtTrgtMaxAge.lt(age)))
                        )
        );
    }

    /** 지역 조건: 해당 지역 OR 지역 미지정 */
    private com.querydsl.core.types.dsl.BooleanExpression buildRegionCondition(String regionCode) {
        var hasMatchingRegion = p.id.in(
                JPAExpressions.select(pr.policy.id).from(pr).where(pr.region.code.eq(regionCode))
        );
        var hasNoRegion = p.id.notIn(
                JPAExpressions.select(pr.policy.id).from(pr)
        );
        return hasMatchingRegion.or(hasNoRegion);
    }

    /** 프로필 조건을 통과하지 못하는 정책 ID를 IN 서브쿼리로 반환 */
    private com.querydsl.core.types.dsl.BooleanExpression buildProfileExclusionCondition(
            Integer age, EmploymentStatus employmentStatus,
            EducationLevel educationLevel, MajorField major) {

        BooleanBuilder exclusion = new BooleanBuilder();
        exclusion.or(q.sprtTrgtMinAge.isNotNull().and(q.sprtTrgtMinAge.gt(age)));
        exclusion.or(q.sprtTrgtMaxAge.isNotNull().and(q.sprtTrgtMaxAge.lt(age)));
        if (employmentStatus != null) {
            exclusion.or(q.jobCd.isNotNull().and(q.jobCd.ne(employmentStatus)));
        }
        if (educationLevel != null) {
            exclusion.or(q.schoolCd.isNotNull().and(q.schoolCd.ne(educationLevel)));
        }
        if (major != null) {
            exclusion.or(q.plcyMajorCd.isNotNull().and(q.plcyMajorCd.ne(major)));
        }

        return p.id.in(
                JPAExpressions.select(q.policy.id)
                        .from(q)
                        .where(exclusion)
        );
    }
}
