package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.BragLogService;
import com.bearpoints.api.service.impl.BragLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BragLogService} implementation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Service implementation and functionality</li>
 *     <li>Data validation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *     <li>Student (invalid)</li>
 *     <li>Teacher (invalid, invalid student)</li>
 *     <li>Behaviors (empty, invalid)</li>
 *  </ul>
 *
 * @see BragLogService
 * @see BragLogServiceImpl
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class BragLogServiceTests {
    /** Mock brag log repository */
    @Mock
    private BragLogDAO bragLogRepository;

    /** Mock student repository */
    @Mock
    private StudentDAO studentRepository;

    /** Mock teacher repository */
    @Mock
    private TeacherDAO teacherRepository;

    /** Mock behavior type repository */
    @Mock
    private BehaviorTypeDAO behaviorTypeRepository;

    /** Injects mock repositories into service implementation */
    @InjectMocks
    private BragLogServiceImpl bragLogService;

    /** Test data */
    private BragLogRequest bragLogRequest;
    private Teacher teacher;
    private Student student;
    private BehaviorType behaviorType;
    private BragLog bragLog;

    @BeforeEach
    public void setup() {
        String notes = "test notes";
        teacher = createValidTeacher("valid.teacher@okcps.org", 1L, 1L);
        student = createValidStudent(teacher);
        behaviorType = createValidBehaviorType();
        bragLog = createValidBragLog(student, teacher, Set.of(behaviorType), notes);
        bragLogRequest = createBragLogRequest(student, teacher, Set.of(behaviorType.getId()), notes);
    }

    private BehaviorType createValidBehaviorType() {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(1L);
        behaviorType.setName("valid behavior type");
        return behaviorType;
    }

    private Teacher createValidTeacher(String email, Long userId, Long teacherId) {
        User teacherUser = new User();
        teacherUser.setId(userId);
        teacherUser.setEmail(email);
        teacherUser.setFirstName("ValidFirstName");
        teacherUser.setLastName("ValidLastName");
        teacherUser.setRole(Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.PRE_K);
        return teacher;
    }

    private Student createValidStudent(Teacher teacher) {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("valid.student@okcps.org");
        studentUser.setFirstName("ValidFirstName");
        studentUser.setLastName("ValidLastName");
        studentUser.setRole(Role.STUDENT);
        Student student = new Student();
        student.setId(1L);
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        return student;
    }

    private BragLog createValidBragLog(Student student, Teacher teacher, Set<BehaviorType> behaviorTypes, String notes) {
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(behaviorTypes);
        bragLog.setPointsGenerated(behaviorType.getPointValue());
        bragLog.setNotes(notes);
        return bragLog;
    }

    private BragLogRequest createBragLogRequest(Student student, Teacher teacher, Set<Long> behaviorTypes, String notes) {
        return new BragLogRequest(
                student.getId(),
                teacher.getId(),
                behaviorTypes,
                notes
        );
    }

    /** Test valid student */
    @Test
    @DisplayName("Valid student passes validation")
    public void validStudentPassesValidation() {
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(behaviorTypeRepository.findById(behaviorType.getId())).thenReturn(Optional.of(behaviorType));
        when(bragLogRepository.save(bragLog)).thenReturn(bragLog);
        assertThat(bragLogService.submitBragLog(bragLogRequest)).isNotNull();
    }

    /** Test invalid student */
    @Test
    @DisplayName("Invalid student fails validation")
    public void invalidStudentFailsValidation() {
        when(studentRepository.findById(student.getId())).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                bragLogService.submitBragLog(bragLogRequest)
        );
        assertEquals("Invalid student ID", exception.getMessage());
    }

    @Nested
    @DisplayName("Tests teacher validation")
    class teacherValidation {
        /** Test invalid teacher */
        @Test
        @DisplayName("Invalid teacher fails validation")
        public void invalidTeacherFailsValidation() {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    bragLogService.submitBragLog(bragLogRequest)
            );
            assertEquals("Invalid teacher ID", exception.getMessage());
        }

        /** Test invalid student - teacher relationship */
        @Test
        @DisplayName("Invalid student - teacher relationship fails validation")
        public void invalidTeacherRelationshipFailsValidation() {
            Teacher otherTeacher = createValidTeacher("other.teacher@okcps.org", 3L, 2L);
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(otherTeacher));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    bragLogService.submitBragLog(bragLogRequest)
            );
            assertEquals("Teacher does not teach this student", exception.getMessage());
        }
    }

    /** Test behaviors validation */
    @Nested
    @DisplayName("Tests behaviors validation")
    class behaviorsValidation {
        /** Test empty behaviors */
        @Test
        @DisplayName("Empty behaviors fail validation")
        public void emptyBehaviorsFailValidation() {
            bragLogRequest = createBragLogRequest(student, teacher, Set.of(), "test notes");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    bragLogService.submitBragLog(bragLogRequest)
            );
            assertEquals("At least one behavior must be selected", exception.getMessage());
        }

        /** Test invalid behaviors */
        @Test
        @DisplayName("Invalid behaviors fail validation")
        public void invalidBehaviorsFailValidation() {
            when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
            when(behaviorTypeRepository.findById(behaviorType.getId())).thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    bragLogService.submitBragLog(bragLogRequest)
            );
            assertEquals("Invalid behavior ID: " + behaviorType.getId(), exception.getMessage());
        }
    }
}
