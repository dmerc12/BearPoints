package com.bearpoints.api.specification;

import com.bearpoints.api.dto.BehaviorTypeSearchCriteria;
import com.bearpoints.api.entity.BehaviorType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic behavior type queries.
 *
 * <p>Translates {@link BehaviorTypeSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields.
 * All conditions are combined using AND logic.
 *
 * @see BehaviorTypeSearchCriteria
 * @see Specification
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class BehaviorTypeSpecification {
    /**
     * Creates a JPA Specification for behavior type entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.
     *
     * @param criteria Search criteria containing filter values
     * @return JPA specification that can be used with Behavior Type DAO queries
     */
    public static Specification<BehaviorType> withCriteria(BehaviorTypeSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Name filter
            if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"
                ));
            }
            // Active filter
            if (criteria.getActive() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("active"),
                        criteria.getActive()
                ));
            }
            // Point value range filters
            if (criteria.getMinPointValue() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("pointValue"),
                        criteria.getMinPointValue()
                ));
            }
            if (criteria.getMaxPointValue() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("pointValue"),
                        criteria.getMaxPointValue()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
