package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.StudentReward;
import com.bearpoints.api.utility.SpecificationUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic student reward queries.
 *
 * <p>Translates {@link StudentRewardSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive and partial matching for text fields.
 * All conditions are combined using AND logic.
 *
 * @see StudentRewardSearchCriteria
 * @see Specification
 * @version 1.2
 * @author Dylan Mercer
 */
@Component
public class StudentRewardSpecification {
    /**
     * Creates a JPA Specification for student reward entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.
     *
     * @param criteria Search criteria containing filter values
     * @return JPA specification that can be used with Student Reward DAO queries
     */
    public static Specification<StudentReward> withCriteria(StudentRewardSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Student name or ID filter
            if (criteria.getStudentName() != null || criteria.getStudentId() != null) {
                Join<StudentReward, Student> studentJoin = root.join("student");
                SpecificationUtils.addStudentNameIdFilters(
                        studentJoin,
                        criteria.getStudentName(),
                        criteria.getStudentId(),
                        predicates,
                        criteriaBuilder
                );
            }
            // Item name or ID filter
            if (criteria.getItemName() != null || criteria.getItemId() != null ||
                    criteria.getMinPointsUsed() != null || criteria.getMaxPointsUsed() != null) {
                Join<StudentReward, RewardItem> itemJoin = root.join("rewardItem");
                // Item name filter
                if (SpecificationUtils.isNotBlank(criteria.getItemName())) {
                    predicates.add(SpecificationUtils
                            .likeIgnoreCase(itemJoin.get("name"), criteria.getItemName(), criteriaBuilder));
                }
                // Item ID filter
                if (criteria.getItemId() != null) {
                    predicates.add(SpecificationUtils
                            .equal(itemJoin.get("id"), criteria.getItemId(), criteriaBuilder));
                }
                // Min points used filter
                if (criteria.getMinPointsUsed() != null) {
                    predicates.add(SpecificationUtils
                            .greaterThanOrEqualTo(itemJoin.get("pointCost"), criteria.getMinPointsUsed(), criteriaBuilder));
                }
                // Max points used filter
                if (criteria.getMaxPointsUsed() != null) {
                    predicates.add(SpecificationUtils
                            .lessThanOrEqualTo(itemJoin.get("pointCost"), criteria.getMaxPointsUsed(), criteriaBuilder));
                }
            }
            // Start date filter
            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("redeemedAt"),
                        criteria.getStartDate()
                ));
            }
            // End date filter
            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("redeemedAt"),
                        criteria.getEndDate()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
