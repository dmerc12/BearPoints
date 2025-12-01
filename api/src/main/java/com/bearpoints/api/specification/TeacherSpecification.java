package com.bearpoints.api.specification;

import com.bearpoints.api.dto.TeacherSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic student queries.
 *
 * <p>Translates {@link TeacherSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields.
 * All conditions are combined using AND logic.
 *
 * @see TeacherSearchCriteria
 * @see Specification
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class TeacherSpecification {
    /**
     * Creates a JPA Specification for teacher entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.</p>
     * @param criteria Search criteria containing filter values
     * @return JPA Specification that can be used with Teacher DAO queries
     */
    public static Specification<Teacher> withCriteria(TeacherSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("role"), Role.TEACHER));
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
            // Grade filter
            if (criteria.getGrade() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("grade"),
                        criteria.getGrade()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean isValidSearchString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
