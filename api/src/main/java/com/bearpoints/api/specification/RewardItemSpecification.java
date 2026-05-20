package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.utility.SpecificationUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic reward item queries.
 *
 * <p>Translates {@link RewardItemSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields.
 * All conditions are combined using AND logic.
 *
 * @see RewardItemSearchCriteria
 * @see Specification
 * @version 1.1
 * @author Dylan Mercer
 */
@Component
public class RewardItemSpecification {
    /**
     * Creates a JPA Specification for reward item entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.
     *
     * @param criteria Search criteria containing filter values
     * @return JPA specification that can be used with Reward Item DAO queries
     */
    public static Specification<RewardItem> withCriteria(RewardItemSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Name filter
            if (SpecificationUtils.isNotBlank(criteria.getName())) {
                predicates.add(SpecificationUtils
                        .likeIgnoreCase(root.get("name"), criteria.getName(), criteriaBuilder));
            }
            // Point cost range filters
            if (criteria.getMinPointCost() != null) {
                predicates.add(SpecificationUtils
                        .greaterThanOrEqualTo(root.get("pointCost"), criteria.getMinPointCost(), criteriaBuilder));
            }
            if (criteria.getMaxPointCost() != null) {
                predicates.add(SpecificationUtils
                        .lessThanOrEqualTo(root.get("pointCost"), criteria.getMaxPointCost(), criteriaBuilder));
            }
            // Stock range filter
            if (criteria.getMinStock() != null) {
                predicates.add(SpecificationUtils
                        .greaterThanOrEqualTo(root.get("stock"), criteria.getMinStock(), criteriaBuilder));
            }
            if (criteria.getMaxStock() != null) {
                predicates.add(SpecificationUtils
                        .lessThanOrEqualTo(root.get("stock"), criteria.getMaxStock(), criteriaBuilder));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
