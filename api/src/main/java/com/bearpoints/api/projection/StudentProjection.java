package com.bearpoints.api.projection;

import com.bearpoints.api.entity.Student;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for condensed student information.
 * <p>
 * Provides a summary view of Student entities with essential fields only.
 * Used in APIs where full student details with brag logs are not required.
 * <p>Fields:
 * <ul>
 *     <li>id - Unique student identifier</li>
 *     <li>points - Current point total</li>
 *     <li>token - Unique access token</li>
 *     <li>user - Associated user details via UserProjection</li>
 *     <li>teacher - Assigned teacher details via TeacherProjection</li>
 * </ul>
 * @see UserProjection
 * @see TeacherProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "studentProjection", types = Student.class)
public interface StudentProjection {
    Long getId();
    Integer getPoints();
    String getToken();
    UserProjection getUser();
    TeacherProjection getTeacher();
}
