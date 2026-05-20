package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.StudentSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.utility.SpecificationUtils;
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
 * @version 1.2
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
            predicates.add(criteriaBuilder.equal(root.get("user").get("role"), Role.STUDENT));
            // User text filters (email, first name, last name)
            SpecificationUtils.addUserTextFilters(
                    root.get("user"),
                    criteria.getEmail(),
                    criteria.getFirstName(),
                    criteria.getLastName(),
                    predicates,
                    criteriaBuilder
            );
            // Teacher filter
            if (criteria.getTeacherId() != null) {
                predicates.add(SpecificationUtils.equal(root.get("teacher").get("id"), criteria.getTeacherId(), criteriaBuilder));
            }
            // Points range filters
            if (criteria.getMinPoints() != null) {
                predicates.add(SpecificationUtils.greaterThanOrEqualTo(root.get("points"), criteria.getMinPoints(), criteriaBuilder));
            }
            if (criteria.getMaxPoints() != null) {
                predicates.add(SpecificationUtils.lessThanOrEqualTo(root.get("points"), criteria.getMaxPoints(), criteriaBuilder));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
