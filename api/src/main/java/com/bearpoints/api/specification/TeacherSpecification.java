package com.bearpoints.api.specification;

import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.utility.SpecificationUtils;
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
 * @version 1.3
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
            // User text filters (email, first name, last name)
            SpecificationUtils.addUserTextFilters(
                    root.get("user"),
                    criteria.getEmail(),
                    criteria.getFirstName(),
                    criteria.getLastName(),
                    predicates,
                    criteriaBuilder
            );
            // Grade filter
            if (criteria.getGrade() != null) {
                predicates.add(SpecificationUtils.equal(root.get("grade"), criteria.getGrade(), criteriaBuilder));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
