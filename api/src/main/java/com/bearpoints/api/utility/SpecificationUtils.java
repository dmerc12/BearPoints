package com.bearpoints.api.utility;

import com.bearpoints.api.entity.User;
import jakarta.persistence.criteria.*;

/**
 * Utility class for building common JPA criteria predicates.
 * <p>Provides methods to reduce code duplication across specific classes.
 * All methods are static and stateless.
 *
 * @version 1.1
 * @author Dylan Mercer
 */
public class SpecificationUtils {
    private SpecificationUtils() {}

    /**
     * Checks if a string is not null and not blank (after trimming).
     *
     * @param value the string to check
     * @return true if the string is non-null and contains non-whitespace characters
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Creates a case-insensitive LIKE predicate for a text field.
     * The pattern is wrapped with {@code %} on both sides.
     *
     * @param path the path to the text field (e.g., {@code root.get("email")})
     * @param value the search term (will be converted to lower case)
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code LOWER(path) LIKE '%value%'}
     */
    public static Predicate likeIgnoreCase(Path<String> path, String value, CriteriaBuilder cb) {
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }

    /**
     * Creates an equality predicate.
     *
     * @param path the path to the field
     * @param value the value to compare (allowed to be {@code null})
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code path = value}
     */
    public static Predicate equal(Path<?> path, Object value, CriteriaBuilder cb) {
        return cb.equal(path, value);
    }

    /**
     * Creates a {@code >=} predicate for a numeric field.
     * The path is converted to a {@link Number} expression to allow a generic {@link Number} value.
     *
     * @param path the path to the numeric field (e.g., {@code root.get("points")})
     * @param value the lower bound (inclusive)
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code path >= value}
     */
    public static Predicate greaterThanOrEqualTo(Path<? extends Number> path, Number value, CriteriaBuilder cb) {
        return cb.ge(path, value);
    }

    /**
     * Creates a {@code <=} predicate for a numeric field.
     * The path is converted to a {@link Number} expression to allow a generic {@link Number} value.
     *
     * @param path the path to the numeric field (e.g., {@code root.get("points")})
     * @param value the upper bound (inclusive)
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code path <= value}
     */
    public static Predicate lessThanOrEqualTo(Path<? extends Number> path, Number value, CriteriaBuilder cb) {
        return cb.le(path, value);
    }

    /**
     * Creates a case-insensitive LIKE predicate that searches the full name of a {@link User}.
     * The full name is built as {@code firstName + " " + lastName}.
     *
     * @param userJoin the join path to the {@link User} entity
     * @param fullName the search term (e.g., "John Doe")
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code LOWER(CONCAT(firstName, ' ', lastName)) LIKE '%fullName%'}
     */
    public static Predicate fullNameLikeIgnoreCase(Join<?, User> userJoin, String fullName, CriteriaBuilder cb) {
        Expression<String> fullNameExpr = cb.concat(
                cb.concat(userJoin.get("firstName"), " "),
                userJoin.get("lastName")
        );
        return cb.like(cb.lower(fullNameExpr), "%" + fullName.toLowerCase() + "%");
    }
}
