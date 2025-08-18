package com.bearpoints.api.dto;

import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StudentSummary} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all student fields</li>
 *     <li>Proper handling of nested UserSummary and TeacherSummary projections</li>
 *     <li>Graceful handling of missing relationships</li>
 * </ul>
 * @see StudentSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Student Summary Tests")
public class StudentSummaryTests {
    /**
     * Tests complete student data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields (id, points, token) are correctly projected</li>
     *     <li>Nested user projection contains all expected values</li>
     *     <li>Nested teacher projection contains all expected values</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all student fields with nested projections")
    void shouldReturnCorrectStudentSummary() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STUDENT);
        User teacherUser = new User();
        teacherUser.setId(2L);
        teacherUser.setEmail("jane.doe@example.com");
        teacherUser.setFirstName("Jane");
        teacherUser.setLastName("Doe");
        teacherUser.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setGrade(GradeLevel.FIRST);
        teacher.setUser(teacherUser);
        Student student = new Student();
        student.setId(1L);
        student.setPoints(150);
        student.generateToken();
        student.setTeacher(teacher);
        student.setUser(user);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        StudentSummary projection = factory.createProjection(StudentSummary.class, student);
        assertEquals(student.getId(), projection.getId());
        assertEquals(student.getPoints(), projection.getPoints());
        assertEquals(student.getToken(), projection.getToken());
        UserSummary userSummary = projection.getUser();
        assertNotNull(userSummary);
        assertEquals(user.getId(), userSummary.getId());
        assertEquals(user.getEmail(), userSummary.getEmail());
        assertEquals(user.getFirstName(), userSummary.getFirstName());
        assertEquals(user.getLastName(), userSummary.getLastName());
        assertEquals(user.getRole().name(), userSummary.getRole());
        TeacherSummary teacherSummary = projection.getTeacher();
        assertNotNull(teacherSummary);
        assertEquals(teacher.getId(), teacherSummary.getId());
        assertEquals(teacher.getGrade(), teacherSummary.getGrade());
        UserSummary teacherUserSummary = teacherSummary.getUser();
        assertNotNull(teacherUserSummary);
        assertEquals(teacherUser.getId(), teacherUserSummary.getId());
        assertEquals(teacherUser.getEmail(), teacherUserSummary.getEmail());
        assertEquals(teacherUser.getFirstName(), teacherUserSummary.getFirstName());
        assertEquals(teacherUser.getLastName(), teacherUserSummary.getLastName());
        assertEquals(teacherUser.getRole().name(), teacherUserSummary.getRole());
    }

    /**
     * Tests student with missing relationships.
     * <p>Verifies:
     * <ul>
     *     <li>Direct fields are still projected correctly</li>
     *     <li>User field returns null when not set</li>
     *     <li>Teacher field returns null when not set</li>
     *     <li>No exceptions are thrown</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle missing relationships gracefully")
    void shouldHandleMissingRelationships() {
        Student student = new Student();
        student.setId(1L);
        student.setPoints(150);
        student.generateToken();
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        StudentSummary projection = factory.createProjection(StudentSummary.class, student);
        assertEquals(student.getId(), projection.getId());
        assertEquals(student.getPoints(), projection.getPoints());
        assertEquals(student.getToken(), projection.getToken());
        assertNull(projection.getUser());
        assertNull(projection.getTeacher());
    }
}
