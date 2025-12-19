package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification implementation for dynamic brag log queries.
 *
 * <p>Translates {@link BragLogSearchCriteria} into JPA criteria predicates
 * for building dynamic database queries with flexible filtering.
 *
 * <p>Implements case-insensitive and partial matching for text fields.
 * All conditions are combined using AND logic.
 *
 * @see BragLogSearchCriteria
 * @see Specification
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class BragLogSpecification {
    /**
     * Creates a JPA Specification for brag log entities based on search criteria.
     * <p>Builds predicates for each non-null filter in the search criteria and combines
     * them using AND logic. Supports partial matching for text fields.
     *
     * @param criteria Search criteria containing filter values
     * @return JPA specification that can be used with Brag Log DAO queries
     */
    public static Specification<BragLog> withCriteria(BragLogSearchCriteria criteria) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Student name or ID filter
            if (criteria.getStudentName() != null || criteria.getStudentId() != null) {
                Join<BragLog, Student> studentJoin = root.join("student");
                // Student name filter
                if (criteria.getStudentName() != null && !criteria.getStudentName().trim().isEmpty()) {
                    Join<Student, User> userStudentJoin = studentJoin.join("user");
                    Expression<String> studentFullName = criteriaBuilder.concat(
                            criteriaBuilder.concat(userStudentJoin.get("firstName"), " "),
                            userStudentJoin.get("lastName")
                    );
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(studentFullName),
                            "%" + criteria.getStudentName().toLowerCase() + "%"
                    ));
                }
                // Student ID filter
                if (criteria.getStudentId() != null) {
                    predicates.add(criteriaBuilder.equal(
                            studentJoin.get("id"),
                            criteria.getStudentId()
                    ));
                }
            }
            // Teacher name or ID filter
            if (criteria.getTeacherName() != null || criteria.getTeacherId() != null) {
                Join<BragLog, Teacher> teacherJoin = root.join("teacher");
                // Teacher name filter
                if (criteria.getTeacherName() != null && !criteria.getTeacherName().trim().isEmpty()) {
                    Join<Teacher, User> userTeacherJoin = teacherJoin.join("user");
                    Expression<String> teacherFullName = criteriaBuilder.concat(
                            criteriaBuilder.concat(userTeacherJoin.get("firstName"), " "),
                            userTeacherJoin.get("lastName")
                    );
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(teacherFullName),
                            "%" + criteria.getTeacherName().toLowerCase() + "%"
                    ));
                }
                // Teacher ID filter
                if (criteria.getTeacherId() != null) {
                    predicates.add(criteriaBuilder.equal(
                            teacherJoin.get("id"),
                            criteria.getTeacherId()
                    ));
                }
            }

            // Grade level filter
            if (criteria.getGrade() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("grade"),
                        criteria.getGrade()
                ));
            }
            // Min points filter
            if (criteria.getMinPoints() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("pointsGenerated"),
                        criteria.getMinPoints()
                ));
            }
            // Max points filter
            if (criteria.getMaxPoints() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("pointsGenerated"),
                        criteria.getMaxPoints()
                ));
            }
            // Start date filter
            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("timestamp"),
                        criteria.getStartDate()
                ));
            }
            // End date filter
            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("timestamp"),
                        criteria.getEndDate()
                ));
            }
            // Notes filter
            if (criteria.getNotes() != null && !criteria.getNotes().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("notes")),
                        "%" + criteria.getNotes().toLowerCase() + "%"
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
