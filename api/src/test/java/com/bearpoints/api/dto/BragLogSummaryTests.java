package com.bearpoints.api.dto;

import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BragLogSummary} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all brag log fields</li>
 *     <li>Proper handling of nested projections</li>
 *     <li>Graceful handling of missing relationships</li>
 *     <li>Correct timestamp mapping</li>
 * </ul>
 * @see BragLogSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Brag Log Summary Tests")
public class BragLogSummaryTests {
    /**
     * Tests complete brag log data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields are correctly projected</li>
     *     <li>Nested student projection is correct</li>
     *     <li>Nested teacher projection is correct</li>
     *     <li>Behavior collection is properly projected</li>
     *     <li>Timestamp is preserved</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all brag log fields with nested projections")
    void shouldReturnCorrectBragLogSummary() {
        User studentUser = new User();
        studentUser.setId(1L);
        studentUser.setEmail("john.doe@example.com");
        studentUser.setFirstName("John");
        studentUser.setLastName("Doe");
        studentUser.setRole(Role.STUDENT);
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
        student.generateToken();
        student.setPoints(160);
        student.setUser(studentUser);
        student.setTeacher(teacher);
        BehaviorType behavior1 = new BehaviorType();
        behavior1.setId(1L);
        behavior1.setName("Helping Others");
        behavior1.setPointValue(3);
        BehaviorType behavior2 = new BehaviorType();
        behavior2.setId(2L);
        behavior2.setName("Participation");
        behavior2.setPointValue(2);
        BragLog bragLog = new BragLog();
        bragLog.setId(1L);
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(Set.of(behavior1, behavior2));
        bragLog.setPointsGenerated(5);
        bragLog.setNotes("Great teamwork during class project");
        bragLog.setTimestamp(LocalDateTime.now());
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BragLogSummary projection = factory.createProjection(BragLogSummary.class, bragLog);
        assertEquals(bragLog.getId(), projection.getId());
        assertEquals(bragLog.getPointsGenerated(), projection.getPointsGenerated());
        assertEquals(bragLog.getNotes(), projection.getNotes());
        assertEquals(bragLog.getTimestamp(), projection.getTimestamp());
        StudentSummary studentSummary = projection.getStudent();
        assertNotNull(studentSummary);
        assertEquals(student.getId(), studentSummary.getId());
        assertEquals(student.getPoints(), studentSummary.getPoints());
        TeacherSummary studentTeacherSummary = studentSummary.getTeacher();
        assertNotNull(studentTeacherSummary);
        assertEquals(teacher.getId(), studentTeacherSummary.getId());
        assertEquals(teacher.getGrade(), studentTeacherSummary.getGrade());
        UserSummary studentTeacherUserSummary = studentSummary.getUser();
        assertNotNull(studentTeacherUserSummary);
        assertEquals(studentUser.getId(), studentTeacherUserSummary.getId());
        assertEquals(studentUser.getEmail(), studentTeacherUserSummary.getEmail());
        assertEquals(studentUser.getFirstName(), studentTeacherUserSummary.getFirstName());
        assertEquals(studentUser.getLastName(), studentTeacherUserSummary.getLastName());
        assertEquals(studentUser.getRole().name(), studentTeacherUserSummary.getRole());
        UserSummary studentUserSummary = studentSummary.getUser();
        assertNotNull(studentUserSummary);
        assertEquals(studentUser.getId(), studentUserSummary.getId());
        assertEquals(studentUser.getEmail(), studentUserSummary.getEmail());
        assertEquals(studentUser.getFirstName(), studentUserSummary.getFirstName());
        assertEquals(studentUser.getLastName(), studentUserSummary.getLastName());
        assertEquals(studentUser.getRole().name(), studentUserSummary.getRole());
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
        Set<BehaviorTypeSummary> behaviorSummaries = projection.getBehaviors();
        assertNotNull(behaviorSummaries);
        assertEquals(2, behaviorSummaries.size());
        behaviorSummaries.forEach(behaviorSummary -> {
            assertTrue(Objects.equals(behaviorSummary.getId(), behavior1.getId()) ||
                    Objects.equals(behaviorSummary.getId(), behavior2.getId()));
            if (Objects.equals(behaviorSummary.getId(), behavior1.getId())) {
                assertEquals(behavior1.getName(), behaviorSummary.getName());
                assertEquals(behavior1.getPointValue(), behaviorSummary.getPointValue());
            } else {
                assertEquals(behavior2.getName(), behaviorSummary.getName());
                assertEquals(behavior2.getPointValue(), behaviorSummary.getPointValue());
            }
        });
    }

    /**
     * Tests brag log with minimum data.
     * <p>Verifies:
     * <ul>
     *     <li>Essential fields are projected correctly</li>
     *     <li>Notes field returns null when not set</li>
     *     <li>Relationships are properly handled</li>
     *     <li>Timestamp is preserved</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle minimal brag log data")
    void shouldHandleMinimalData() {
        Student student = new Student();
        student.setId(1L);
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        BehaviorType behavior = new BehaviorType();
        behavior.setId(1L);
        behavior.setName("Helping Others");
        behavior.setPointValue(3);
        BragLog bragLog = new BragLog();
        bragLog.setId(1L);
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(Set.of(behavior));
        bragLog.setPointsGenerated(3);
        bragLog.setTimestamp(LocalDateTime.now());
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BragLogSummary projection = factory.createProjection(BragLogSummary.class, bragLog);
        assertEquals(bragLog.getId(), projection.getId());
        assertEquals(bragLog.getPointsGenerated(), projection.getPointsGenerated());
        assertEquals(bragLog.getNotes(), projection.getNotes());
        assertEquals(bragLog.getTimestamp(), projection.getTimestamp());
        assertNotNull(projection.getStudent());
        assertNotNull(projection.getTeacher());
        Set<BehaviorTypeSummary> behaviors = projection.getBehaviors();
        assertEquals(1, behaviors.size());
        BehaviorTypeSummary behaviorSummary = behaviors.iterator().next();
        assertEquals(behavior.getId(), behaviorSummary.getId());
        assertEquals(behavior.getName(), behaviorSummary.getName());
    }
}
