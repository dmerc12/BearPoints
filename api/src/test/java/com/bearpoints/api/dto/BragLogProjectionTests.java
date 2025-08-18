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
 * Unit tests for {@link BragLogProjection} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all brag log fields</li>
 *     <li>Proper handling of nested projections</li>
 *     <li>Graceful handling of missing relationships</li>
 *     <li>Correct timestamp mapping</li>
 * </ul>
 * @see BragLogProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Brag Log Projection Tests")
public class BragLogProjectionTests {
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
    void shouldReturnCorrectBragLogProjection() {
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
        BragLogProjection projection = factory.createProjection(BragLogProjection.class, bragLog);
        assertEquals(bragLog.getId(), projection.getId());
        assertEquals(bragLog.getPointsGenerated(), projection.getPointsGenerated());
        assertEquals(bragLog.getNotes(), projection.getNotes());
        assertEquals(bragLog.getTimestamp(), projection.getTimestamp());
        StudentProjection studentProjection = projection.getStudent();
        assertNotNull(studentProjection);
        assertEquals(student.getId(), studentProjection.getId());
        assertEquals(student.getPoints(), studentProjection.getPoints());
        TeacherProjection studentTeacherProjection = studentProjection.getTeacher();
        assertNotNull(studentTeacherProjection);
        assertEquals(teacher.getId(), studentTeacherProjection.getId());
        assertEquals(teacher.getGrade(), studentTeacherProjection.getGrade());
        UserProjection studentTeacherUserProjection = studentProjection.getUser();
        assertNotNull(studentTeacherUserProjection);
        assertEquals(studentUser.getId(), studentTeacherUserProjection.getId());
        assertEquals(studentUser.getEmail(), studentTeacherUserProjection.getEmail());
        assertEquals(studentUser.getFirstName(), studentTeacherUserProjection.getFirstName());
        assertEquals(studentUser.getLastName(), studentTeacherUserProjection.getLastName());
        assertEquals(studentUser.getRole().name(), studentTeacherUserProjection.getRole());
        UserProjection studentUserProjection = studentProjection.getUser();
        assertNotNull(studentUserProjection);
        assertEquals(studentUser.getId(), studentUserProjection.getId());
        assertEquals(studentUser.getEmail(), studentUserProjection.getEmail());
        assertEquals(studentUser.getFirstName(), studentUserProjection.getFirstName());
        assertEquals(studentUser.getLastName(), studentUserProjection.getLastName());
        assertEquals(studentUser.getRole().name(), studentUserProjection.getRole());
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
        Set<BehaviorTypeProjection> behaviorProjections = projection.getBehaviors();
        assertNotNull(behaviorProjections);
        assertEquals(2, behaviorProjections.size());
        behaviorProjections.forEach(behaviorProjection -> {
            assertTrue(Objects.equals(behaviorProjection.getId(), behavior1.getId()) ||
                    Objects.equals(behaviorProjection.getId(), behavior2.getId()));
            if (Objects.equals(behaviorProjection.getId(), behavior1.getId())) {
                assertEquals(behavior1.getName(), behaviorProjection.getName());
                assertEquals(behavior1.getPointValue(), behaviorProjection.getPointValue());
            } else {
                assertEquals(behavior2.getName(), behaviorProjection.getName());
                assertEquals(behavior2.getPointValue(), behaviorProjection.getPointValue());
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
        BragLogProjection projection = factory.createProjection(BragLogProjection.class, bragLog);
        assertEquals(bragLog.getId(), projection.getId());
        assertEquals(bragLog.getPointsGenerated(), projection.getPointsGenerated());
        assertEquals(bragLog.getNotes(), projection.getNotes());
        assertEquals(bragLog.getTimestamp(), projection.getTimestamp());
        assertNotNull(projection.getStudent());
        assertNotNull(projection.getTeacher());
        Set<BehaviorTypeProjection> behaviors = projection.getBehaviors();
        assertEquals(1, behaviors.size());
        BehaviorTypeProjection behaviorProjection = behaviors.iterator().next();
        assertEquals(behavior.getId(), behaviorProjection.getId());
        assertEquals(behavior.getName(), behaviorProjection.getName());
    }
}
