package com.bearpoints.api.dto;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Teacher;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for condensed teacher information.
 * <p>
 * Provides a summary view of Teacher entities with essential fields only.
 * Used in APIs where full teacher details with students/brag logs are not required.
 *
 * <p>Fields:
 * <ul>
 *     <li>id - Unique teacher identifier</li>
 *     <li>grade - Teacher's assigned grade level</li>
 *     <li>user - Associated user details via UserProjection</li>
 * </ul>
 * @see UserProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "teacherProjection", types = Teacher.class)
public interface TeacherProjection {
    Long getId();
    GradeLevel getGrade();
    UserProjection getUser();
}
