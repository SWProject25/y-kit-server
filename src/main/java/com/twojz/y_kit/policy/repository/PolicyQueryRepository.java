package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyQueryRepository {

    Page<PolicyEntity> findAllActive(Pageable pageable);

    Page<PolicyEntity> findByCategoryId(Long categoryId, Pageable pageable);

    Page<PolicyEntity> findByKeywords(List<String> keywords, Pageable pageable);

    Page<PolicyEntity> findByRegionCode(String regionCode, Pageable pageable);

    Page<PolicyEntity> findByAge(Integer age, Pageable pageable);

    Page<PolicyEntity> findApplicationAvailable(LocalDate today, Pageable pageable);

    Page<PolicyEntity> findByKeywordsAndAge(List<String> keywords, Integer age, Pageable pageable);

    Page<PolicyEntity> findRecommendedWithCategory(Integer age, String regionCode, Pageable pageable);

    Page<PolicyEntity> findRecommendedWithProfile(Integer age, String regionCode,
            EmploymentStatus employmentStatus, EducationLevel educationLevel, MajorField major,
            Pageable pageable);

    Page<PolicyEntity> findPopularByViewCount(Pageable pageable);

    Page<PolicyEntity> findPopularByBookmarkCount(Pageable pageable);

    Page<PolicyEntity> findDeadlineSoon(LocalDate today, Pageable pageable);

    List<PolicyEntity> findSimilarByCategory(Long policyId, int limit);

    Page<PolicyEntity> searchPolicies(List<Long> categoryIds, List<Long> keywordIds,
            List<String> keywords, Pageable pageable);
}
