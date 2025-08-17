package com.bearpoints.api.dto;

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
 * Unit tests for {@link TeacherSummary} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all teacher fields</li>
 *     <li>Proper handling of nested UserSummary projection</li>
 *     <li>Graceful handling of missing user reference</li>
 * </ul>
 * @see TeacherSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Teacher Summary Tests")
public class TeacherSummaryTests {
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
    void shouldReturnCorrectTeacherSummary() {
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
        TeacherSummary projection = factory.createProjection(TeacherSummary.class, teacher);
        assertEquals(teacher.getId(), projection.getId());
        assertEquals(teacher.getGrade(), projection.getGrade());
        UserSummary userSummary = projection.getUser();
        assertNotNull(userSummary);
        assertEquals(user.getId(), userSummary.getId());
        assertEquals(user.getEmail(), userSummary.getEmail());
        assertEquals(user.getFirstName(), userSummary.getFirstName());
        assertEquals(user.getLastName(), userSummary.getLastName());
        assertEquals(user.getRole().name(), userSummary.getRole());
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
        TeacherSummary projection = factory.createProjection(TeacherSummary.class, teacher);
        assertEquals(teacher.getId(), projection.getId());
        assertEquals(teacher.getGrade(), projection.getGrade());
        assertNull(projection.getUser());
    }
}
