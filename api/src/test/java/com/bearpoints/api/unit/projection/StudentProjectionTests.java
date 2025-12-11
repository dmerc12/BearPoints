package com.bearpoints.api.unit.projection;

import com.bearpoints.api.projection.StudentProjection;
import com.bearpoints.api.projection.TeacherProjection;
import com.bearpoints.api.projection.UserProjection;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StudentProjection} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all student fields</li>
 *     <li>Proper handling of nested UserProjection and TeacherProjection projections</li>
 *     <li>Graceful handling of missing relationships</li>
 * </ul>
 * @see StudentProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Student Projection Tests")
public class StudentProjectionTests {
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
    void shouldReturnCorrectStudentProjection() {
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
        StudentProjection projection = factory.createProjection(StudentProjection.class, student);
        assertEquals(student.getId(), projection.getId());
        assertEquals(student.getPoints(), projection.getPoints());
        assertEquals(student.getToken(), projection.getToken());
        UserProjection userProjection = projection.getUser();
        assertNotNull(userProjection);
        assertEquals(user.getId(), userProjection.getId());
        assertEquals(user.getEmail(), userProjection.getEmail());
        assertEquals(user.getFirstName(), userProjection.getFirstName());
        assertEquals(user.getLastName(), userProjection.getLastName());
        assertEquals(user.getRole().name(), userProjection.getRole());
        TeacherProjection teacherProjection = projection.getTeacher();
        assertNotNull(teacherProjection);
        assertEquals(teacher.getId(), teacherProjection.getId());
        assertEquals(teacher.getGrade(), teacherProjection.getGrade());
        UserProjection teacherUserProjection = teacherProjection.getUser();
        assertNotNull(teacherUserProjection);
        assertEquals(teacherUser.getId(), teacherUserProjection.getId());
        assertEquals(teacherUser.getEmail(), teacherUserProjection.getEmail());
        assertEquals(teacherUser.getFirstName(), teacherUserProjection.getFirstName());
        assertEquals(teacherUser.getLastName(), teacherUserProjection.getLastName());
        assertEquals(teacherUser.getRole().name(), teacherUserProjection.getRole());
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
        StudentProjection projection = factory.createProjection(StudentProjection.class, student);
        assertEquals(student.getId(), projection.getId());
        assertEquals(student.getPoints(), projection.getPoints());
        assertEquals(student.getToken(), projection.getToken());
        assertNull(projection.getUser());
        assertNull(projection.getTeacher());
    }
}
