package com.bearpoints.api.utility;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import jakarta.persistence.criteria.*;

import java.util.List;

/**
 * Utility class for building common JPA criteria predicates.
 * <p>Provides methods to reduce code duplication across specific classes.
 * All methods are static and stateless.
 *
 * @version 1.2
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
     * Creates a {@code >=} predicate for any {@link Comparable} field (numbers, dates, etc.).
     *
     * @param path the path to the field (e.g., {@code root.get("points")} or {@code root.get("timestamp")})
     * @param value the lower bound (inclusive)
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code path >= value}
     */
    public static <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Path<Y> path, Y value, CriteriaBuilder cb) {
        return cb.greaterThanOrEqualTo(path, value);
    }

    /**
     * Creates a {@code <=} predicate for any {@link Comparable} field (numbers, dates, etc.).
     *
     * @param path the path to the field (e.g., {@code root.get("points")} or {@code root.get("timestamp")})
     * @param value the upper bound (inclusive)
     * @param cb the {@link CriteriaBuilder}
     * @return a {@link Predicate} equivalent to {@code path <= value}
     */
    public static <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Path<Y> path, Y value, CriteriaBuilder cb) {
        return cb.lessThanOrEqualTo(path, value);
    }

    /**
     * Adds a grade equality filter if the grade is non-null.
     *
     * @param gradePath the path to the grade field (e.g., {@code root.get("grade")}
     * @param grade the grade value to match
     * @param predicates the list to add the predicates to
     * @param cb the {@link CriteriaBuilder}
     */
    public static void addGradeFilter(Path<GradeLevel> gradePath, GradeLevel grade, List<Predicate> predicates, CriteriaBuilder cb) {
        if (grade != null) {
            predicates.add(equal(gradePath, grade, cb));
        }
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

    /**
     * Adds case-insensitive LIKE predicates for email, first name, and last name
     * if the corresponding values are not blank.
     *
     * @param userPath the path to the User entity (e.g., {@code root.get("user")})
     * @param email the email search term
     * @param firstName the first name search term
     * @param lastName the last name search term
     * @param predicates the list to add predicates to
     * @param criteriaBuilder the {@link CriteriaBuilder}
     */
    public static void addUserTextFilters(Path<?> userPath, String email, String firstName, String lastName,
                                          List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        if (isNotBlank(email)) {
            predicates.add(likeIgnoreCase(userPath.get("email"), email, criteriaBuilder));
        }
        if (isNotBlank(firstName)) {
            predicates.add(likeIgnoreCase(userPath.get("firstName"), firstName, criteriaBuilder));
        }
        if (isNotBlank(lastName)) {
            predicates.add(likeIgnoreCase(userPath.get("lastName"), lastName, criteriaBuilder));
        }
    }

    /**
     * Adds student full-name case-insensitive LIKE and student ID equality predicates
     * if the corresponding values are provided.
     *
     * @param studentJoin the join to the Student entity
     * @param studentName the student name search term
     * @param studentId the student ID to match exactly
     * @param predicates the list to add predicates to
     * @param criteriaBuilder the {@link CriteriaBuilder}
     */
    public static void addStudentNameIdFilters(Join<?, Student> studentJoin, String studentName, Long studentId,
                                               List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        if (isNotBlank(studentName)) {
            Join<Student, User> userJoin = studentJoin.join("user");
            predicates.add(fullNameLikeIgnoreCase(userJoin, studentName, criteriaBuilder));
        }
        if (studentId != null) {
            predicates.add(equal(studentJoin.get("id"), studentId, criteriaBuilder));
        }
    }

    public static void addTeacherNameIdFilters(Join<?, Teacher> teacherJoin, String teacherName, Long teacherId,
                                               List<Predicate> predicates, CriteriaBuilder criteriaBuilder) {
        if (isNotBlank(teacherName)) {
            Join<Teacher, User> userJoin = teacherJoin.join("user");
            predicates.add(fullNameLikeIgnoreCase(userJoin, teacherName, criteriaBuilder));
        }
        if (teacherId != null) {
            predicates.add(equal(teacherJoin.get("id"), teacherId, criteriaBuilder));
        }
    }
}
