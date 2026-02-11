package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic user queries.
 *
 * <p>Translates {@link UserSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive partial matching for text fields and exact matching
 * for ID fields. All conditions are combined using AND logic.
 *
 * @see UserSearchCriteria
 * @see Specification
 * @version 2.0
 * @author Dylan Mercer
 */
@Component
public class UserSpecification {
    /**
     * Creates a JPA Specification for user entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.</p>
     * @param criteria Search criteria containing filter values
     * @return JPA Specification that can be used with User DAO queries
     */
    public static Specification<User> withCriteria(UserSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Role filter
            if (criteria.getRole() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("role"),
                        criteria.getRole()
                ));
            }
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

    /**
     * Creates a JPA Specification for exact first and last name match.
     * <p>Useful for finding a specific user by their exact name (case-sensitive).
     *
     * @param firstName Exact first name to match (case-insensitive)
     * @param lastName Exact last name to match (case-insensitive)
     * @return JPA Specification for exact name match
     */
    public static Specification<User> byExactName(String firstName, String lastName) {
        return (root, _, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("firstName")),
                        firstName.toLowerCase()
                ),
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("lastName")),
                        lastName.toLowerCase()
                )
        );
    }

    /**
     * Validates if a search string is non-null and non-empty.
     *
     * @param value The search string to validate
     * @return true if the string is valid for search, false otherwise
     */
    public static boolean isValidSearchString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
