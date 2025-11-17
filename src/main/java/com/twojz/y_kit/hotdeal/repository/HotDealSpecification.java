package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.service.HotDealSearchFilter;
import org.springframework.data.jpa.domain.Specification;

public class HotDealSpecification {

    public static Specification<HotDealEntity> search(HotDealSearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            var predicate = cb.conjunction();

            if (filter.getRegionCode() != null) {
                predicate.getExpressions().add(
                        cb.equal(root.get("region").get("code"), filter.getRegionCode())
                );
            }

            if (filter.getDealType() != null) {
                predicate.getExpressions().add(
                        cb.equal(root.get("dealType"), filter.getDealType())
                );
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicate.getExpressions().add(
                        cb.like(root.get("title"), "%" + filter.getKeyword() + "%")
                );
            }

            return predicate;
        };
    }
}
