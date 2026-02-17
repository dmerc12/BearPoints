package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.utility.SpecificationUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
 * @version 1.1
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
                if (SpecificationUtils.isNotBlank(criteria.getStudentName())) {
                    Join<Student, User> userStudentJoin = studentJoin.join("user");
                    predicates.add(SpecificationUtils
                            .fullNameLikeIgnoreCase(userStudentJoin, criteria.getStudentName(), criteriaBuilder));
                }
                // Student ID filter
                if (criteria.getStudentId() != null) {
                    predicates.add(SpecificationUtils
                            .equal(studentJoin.get("id"), criteria.getStudentId(), criteriaBuilder));
                }
            }
            // Teacher name or ID filter
            if (criteria.getTeacherName() != null || criteria.getTeacherId() != null) {
                Join<BragLog, Teacher> teacherJoin = root.join("teacher");
                // Teacher name filter
                if (SpecificationUtils.isNotBlank(criteria.getTeacherName())) {
                    Join<Teacher, User> userTeacherJoin = teacherJoin.join("user");
                    predicates.add(SpecificationUtils
                            .fullNameLikeIgnoreCase(userTeacherJoin, criteria.getTeacherName(), criteriaBuilder));
                }
                // Teacher ID filter
                if (criteria.getTeacherId() != null) {
                    predicates.add(SpecificationUtils
                            .equal(teacherJoin.get("id"), criteria.getTeacherId(), criteriaBuilder));
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
                predicates.add(SpecificationUtils
                        .greaterThanOrEqualTo(root.get("pointsGenerated"), criteria.getMinPoints(), criteriaBuilder));
            }
            // Max points filter
            if (criteria.getMaxPoints() != null) {
                predicates.add(SpecificationUtils
                        .lessThanOrEqualTo(root.get("pointsGenerated"), criteria.getMaxPoints(), criteriaBuilder));
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
            if (SpecificationUtils.isNotBlank(criteria.getNotes())) {
                predicates.add(SpecificationUtils
                        .likeIgnoreCase(root.get("notes"), criteria.getNotes(), criteriaBuilder));
            }
            // Submitter name filter
            if (SpecificationUtils.isNotBlank(criteria.getSubmitterName())) {
                predicates.add(SpecificationUtils
                        .likeIgnoreCase(root.get("submitterName"), criteria.getSubmitterName(), criteriaBuilder));
            }
            // Submitter user ID filter
            if (criteria.getSubmitterUserId() != null) {
                Join<BragLog, User> submitterUserJoin = root.join("submitterUser", JoinType.LEFT);
                predicates.add(SpecificationUtils
                        .equal(submitterUserJoin.get("id"), criteria.getSubmitterUserId(), criteriaBuilder));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
