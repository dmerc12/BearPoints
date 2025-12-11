package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.AdminSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic admin queries.
 *
 * <p>Translates {@link AdminSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields and exact matching
 * for ID fields. All conditions are combined using AND logic.
 *
 * @see AdminSearchCriteria
 * @see Specification
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class AdminSpecification {
    /**
     * Creates a JPA Specification for admin entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.</p>
     * @param criteria Search criteria containing filter values
     * @return JPA Specification that can be used with Admin DAO queries
     */
    public static Specification<User> withCriteria(AdminSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("role"), Role.ADMIN));
            // Email filter
            if (isValidSearchString(criteria.getEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }
            // First name filter
            if (isValidSearchString(criteria.getFirstName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + criteria.getFirstName().toLowerCase() + "%"
                ));
            }
            // Last name filter
            if (isValidSearchString(criteria.getLastName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + criteria.getLastName().toLowerCase() + "%"
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static boolean isValidSearchString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
