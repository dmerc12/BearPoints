package com.bearpoints.api.unit.projection;

import com.bearpoints.api.projection.TeacherProjection;
import com.bearpoints.api.projection.UserProjection;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TeacherProjection} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all teacher fields</li>
 *     <li>Proper handling of nested UserProjection projection</li>
 *     <li>Graceful handling of missing user reference</li>
 * </ul>
 * @see TeacherProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Teacher Projection Tests")
public class TeacherProjectionTests {
    /**
     * Tests complete teacher data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields (id, grade) are correctly projected</li>
     *     <li>Nested user projection contains all expected values</li>
     *     <li>Role conversion works as expected</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all teacher fields with nested user")
    void shouldReturnCorrectTeacherProjection() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setGrade(GradeLevel.FIRST);
        teacher.setUser(user);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        TeacherProjection projection = factory.createProjection(TeacherProjection.class, teacher);
        assertEquals(teacher.getId(), projection.getId());
        assertEquals(teacher.getGrade(), projection.getGrade());
        UserProjection userProjection = projection.getUser();
        assertNotNull(userProjection);
        assertEquals(user.getId(), userProjection.getId());
        assertEquals(user.getEmail(), userProjection.getEmail());
        assertEquals(user.getFirstName(), userProjection.getFirstName());
        assertEquals(user.getLastName(), userProjection.getLastName());
        assertEquals(user.getRole().name(), userProjection.getRole());
    }

    /**
     * Tests teacher without user reference.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields (id, grade) are still projected</li>
     *     <li>User field returns null when not set</li>
     *     <li>No exceptions are thrown</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle missing user reference gracefully")
    void shouldHandleMissingUser() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setGrade(GradeLevel.FIRST);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        TeacherProjection projection = factory.createProjection(TeacherProjection.class, teacher);
        assertEquals(teacher.getId(), projection.getId());
        assertEquals(teacher.getGrade(), projection.getGrade());
        assertNull(projection.getUser());
    }
}
