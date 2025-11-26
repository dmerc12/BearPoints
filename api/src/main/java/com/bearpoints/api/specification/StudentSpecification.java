package com.bearpoints.api.specification;

import com.bearpoints.api.dto.StudentSearchCriteria;
import com.bearpoints.api.entity.Student;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic student queries.
 *
 * <p>Translates {@link StudentSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields and exact matching
 * for ID fields. All conditions are combined using AND logic.
 *
 * @see StudentSearchCriteria
 * @see Specification
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class StudentSpecification {
    /**
     * Creates a JPA Specification for student entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields and
     * exact matching for ID and numeric fields.</p>
     * @param criteria Search criteria containing filter values
     * @return JPA Specification that can be used with Student DAO queries
     */
    public static Specification<Student> withCriteria(StudentSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Email filter
            if (isValidSearchString(criteria.getEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("email")),
                        "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }
            // First name filter
            if (isValidSearchString(criteria.getFirstName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("firstName")),
                        "%" + criteria.getFirstName().toLowerCase() + "%"
                ));
            }
            // Last name filter
            if (isValidSearchString(criteria.getLastName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("lastName")),
                        "%" + criteria.getLastName().toLowerCase() + "%"
                ));
            }
            // Teacher filter
            if (criteria.getTeacherId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("teacher").get("id"),
                        criteria.getTeacherId()
                ));
            }
            // Points range filters
            if (criteria.getMinPoints() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("points"),
                        criteria.getMinPoints()
                ));
            }
            if (criteria.getMaxPoints() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("points"),
                        criteria.getMaxPoints()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean isValidSearchString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
