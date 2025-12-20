package com.bearpoints.api.unit.service;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.BragLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BragLogServiceImpl}.
 * <p>Verifies brag log management functionality including CRUD operations and
 * search with criteria.
 *
 * @see BragLogServiceImpl
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class BragLogServiceTests {
    @Mock
    private BragLogDAO bragLogDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private BehaviorTypeDAO behaviorTypeDAO;

    @InjectMocks
    private BragLogServiceImpl bragLogService;

    // Deprecated
    private BragLogRequest bragLogRequest;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Teacher teacher;
    private Student student;
    private BehaviorType behaviorType1;
    private BehaviorType behaviorType2;
    private BragLog bragLog;
    private BragLogDTO bragLogDTO;

    @BeforeEach
    public void setup() {
        teacher = createValidTeacher(1L, GradeLevel.FIRST);
        student = createValidStudent(1L, teacher);
        behaviorType1 = createValidBehaviorType(1L, "Helping Others", 2);
        behaviorType2 = createValidBehaviorType(2L, "Participated", 5);
        bragLog = createValidBragLog(student, Set.of(behaviorType1, behaviorType2));
        bragLogDTO = new BragLogDTO(1L, student.getId(), null,
                Set.of(behaviorType1.getId(), behaviorType2.getId()), "test notes 1",
                null, null, null, null, null, null);

        // DEPRECATED
        bragLogRequest = createBragLogRequest(student, teacher, Set.of(behaviorType1.getId(), behaviorType2.getId()));
    }

    @Nested
    @DisplayName("When retrieving all brag logs")
    class WhenRetrievingAllBragLogs {
        @Test
        @DisplayName("Should retrieve all brag logs with pagination")
        void shouldRetrieveAllBragLogsWithPagination() {
            List<BragLog> bragLogs = List.of(bragLog);
            Page<BragLog> bragLogPage = new PageImpl<>(bragLogs, pageable, 1L);
            when(bragLogDAO.findAll(any(Pageable.class))).thenReturn(bragLogPage);
            PagedResponseDTO<BragLogDTO> result = bragLogService.getAllBragLogs(pageable);
            assertNotNull(result);
            assertEquals(1L, result.getContent().size());
            assertEquals(1L, result.getTotalElements());
            verify(bragLogDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no brag logs exist")
        void shouldReturnEmptyPageWhenNoBragLogsExist() {
            Page<BragLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(bragLogDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<BragLogDTO> result = bragLogService.getAllBragLogs(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching brag logs with criteria")
    class WhenSearchingBragLogsWithCriteria {
        @Test
        @DisplayName("Should search brag logs with student name criteria")
        void shouldSearchBragLogsWithStudentNameCriteria() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStudentName("ValidFirstName");
            List<BragLog> bragLogs = List.of(bragLog);
            Page<BragLog> bragLogPage = new PageImpl<>(bragLogs, pageable, 1L);
            when(bragLogDAO.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(bragLogPage);
            PagedResponseDTO<BragLogDTO> result = bragLogService.searchBragLogs(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(bragLogDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all brag logs with no criteria specified")
        void shouldReturnAllBragLogsWhenNoCriteriaSpecified() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            List<BragLog> bragLogs = List.of(bragLog);
            Page<BragLog> bragLogPage = new PageImpl<>(bragLogs, pageable, 1L);
            when(bragLogDAO.findAll(any(Pageable.class))).thenReturn(bragLogPage);
            PagedResponseDTO<BragLogDTO> result = bragLogService.searchBragLogs(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(bragLogDAO).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("When retrieving brag log by ID")
    class WhenRetrievingBragLogById {
        @Test
        @DisplayName("Should return brag log by ID when found")
        void shouldReturnBragLogByIdWhenFound() {
            Long bragLogId = bragLog.getId();
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            BragLogDTO result = bragLogService.getBragLogById(bragLogId);
            assertNotNull(result);
            assertEquals(bragLogId, result.getId());
            assertEquals(student.getId(), result.getStudentId());
            assertEquals(teacher.getId(), result.getTeacherId());
            assertEquals(bragLog.getPointsGenerated(), result.getPointsGenerated());
            verify(bragLogDAO).findById(bragLogId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when brag log not found by ID")
        void shouldThrowResourceNotFoundExceptionWhenBragLogNotFoundById() {
            Long bragLogId = 9999L;
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.getBragLogById(bragLogId));
            verify(bragLogDAO).findById(bragLogId);
        }
    }

    @Nested
    @DisplayName("When creating brag log")
    class WhenCreatingBragLog {
        @Test
        @DisplayName("Should create new brag log successfully")
        void shouldCreateNewBragLogSuccessfully() {
            Set<Long> behaviorIds = bragLogDTO.getBehaviorIds();
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            BragLogDTO result = bragLogService.createBragLog(createDTO);
            assertNotNull(result);
            assertEquals(bragLog.getId(), result.getId());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentNotFound() {
            Long studentId = 9999L;
            Set<Long> behaviorIds = bragLogDTO.getBehaviorIds();
            BragLogDTO createDTO = new BragLogDTO(null, studentId, null,
                    behaviorIds, bragLog.getNotes(), null, null, null,
                    null, null, null);
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.createBragLog(createDTO));
            verify(studentDAO).findById(studentId);
            verify(behaviorTypeDAO, never()).findById(anyLong());
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when behavior type not found")
        void shouldThrowResourceNotFoundExceptionWhenBehaviorTypeNotFound() {
            Long behaviorTypeId = 9999L;
            Set<Long> behaviorIds = Set.of(behaviorTypeId);
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.createBragLog(createDTO));
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }
    }

    @Nested
    @DisplayName("When updating brag log")
    class WhenUpdatingBragLog {
        @Test
        @DisplayName("Should update existing brag log successfully")
        void shouldUpdateExistingBragLogSuccessfully() {
            Long bragLogId = bragLog.getId();
            String updatedNotes = "Updated notes";
            Set<Long> updatedBehaviorIds = Set.of(1L);
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    updatedBehaviorIds, updatedNotes, null, null,
                    null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            BragLogDTO result = bragLogService.updateBragLog(bragLogId, updateDTO);
            assertNotNull(result);
            verify(bragLogDAO).findById(bragLogId);
            verify(studentDAO, never()).findById(anyLong());
            verify(behaviorTypeDAO).findById(behaviorType1.getId());
            verify(bragLogDAO).save(any(BragLog.class));

        }

        @Test
        @DisplayName("Should update student and recalculate when student changes")
        void shouldUpdateStudentAndRecalculateWhenStudentChanges() {
            Long bragLogId = bragLog.getId();
            Teacher newTeacher = createValidTeacher(2L, GradeLevel.SECOND);
            Student newStudent = createValidStudent(2L, newTeacher);
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, newStudent.getId(), newTeacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLogDTO.getNotes(), null, null, null,
                    null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(studentDAO.findById(newStudent.getId())).thenReturn(Optional.of(newStudent));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            BragLogDTO result = bragLogService.updateBragLog(bragLogId, updateDTO);
            assertNotNull(result);
            verify(bragLogDAO).findById(bragLogId);
            verify(studentDAO).findById(newStudent.getId());
            verify(behaviorTypeDAO, never()).findById(anyLong());
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should update notes only when notes changed")
        void shouldUpdateNotesOnlyWhenNotesChanged() {
            Long bragLogId = bragLog.getId();
            String newNotes = "updated notes";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, bragLogDTO.getStudentId(), bragLogDTO.getTeacherId(),
                    bragLogDTO.getBehaviorIds(), newNotes, null, null, null,
                    null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            bragLogService.updateBragLog(bragLogId, updateDTO);
            verify(bragLogDAO).findById(bragLogId);
            verify(studentDAO, never()).findById(anyLong());
            verify(behaviorTypeDAO, never()).findById(anyLong());
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when brag log not found by ID")
        void shouldThrowResourceNotFoundExceptionWhenBragLogNotFoundById() {
            Long bragLogId = 999L;
            Set<Long> newBehaviorIds = Set.of(behaviorType1.getId());
            String newNotes = "updated notes";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(), newBehaviorIds,
                    newNotes, null, null, null, null,
                    null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.updateBragLog(bragLogId, updateDTO));
            verify(bragLogDAO).findById(bragLogId);
            verify(studentDAO, never()).findById(anyLong());
            verify(behaviorTypeDAO, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("When deleting brag log")
    class WhenDeletingBragLog {
        @Test
        @DisplayName("Should delete brag log successfully")
        void shouldDeleteBragLogSuccessfully() {
            Long bragLogId = bragLog.getId();
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            doNothing().when(bragLogDAO).delete(bragLog);
            bragLogService.deleteBragLog(bragLogId);
            verify(bragLogDAO).findById(bragLogId);
            verify(bragLogDAO).delete(bragLog);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when brag log not found by ID")
        void shouldThrowResourceNotFoundExceptionWhenBragLogNotFoundById() {
            Long bragLogId = bragLog.getId();
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.deleteBragLog(bragLogId));
            verify(bragLogDAO).findById(bragLogId);
            verify(bragLogDAO, never()).delete(any(BragLog.class));
        }
    }

    private BehaviorType createValidBehaviorType(Long id, String name, Integer pointValue) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(id);
        behaviorType.setName(name);
        behaviorType.setActive(true);
        behaviorType.setPointValue(pointValue);
        return behaviorType;
    }

    private User createValidUser(Long id, Role role) {
        String firstName = "ValidFirstName";
        String lastName = "ValidLastName";
        User user = new User();
        user.setId(id);
        user.setEmail(firstName + lastName + id + "@okcps.org");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }

    private Teacher createValidTeacher(Long id, GradeLevel grade) {
        User teacherUser = createValidUser(id + 100, Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setId(id);
        teacher.setUser(teacherUser);
        teacher.setGrade(grade);
        return teacher;
    }

    private Student createValidStudent(Long id, Teacher teacher) {
        User studentUser = createValidUser(id + 100000, Role.STUDENT);
        Student student = new Student();
        student.setId(id);
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        return student;
    }

    private BragLog createValidBragLog(Student student, Set<BehaviorType> behaviors) {
        BragLog bragLog = new BragLog();
        bragLog.setId(1L);
        bragLog.setStudent(student);
        bragLog.setBehaviors(behaviors);
        bragLog.setNotes("test notes " + (Long) 1L);
        bragLog.setDefaultsBeforePersist();
        return bragLog;
    }



    // DEPRECATED
    private BragLogRequest createBragLogRequest(Student student, Teacher teacher, Set<Long> behaviorTypes) {
        return new BragLogRequest(
                student.getId(),
                teacher.getId(),
                behaviorTypes,
                "test notes"
        );
    }

    @Nested
    @DisplayName("DEPRECATED method tests")
    class DeprecatedMethodTests {
        /** Test valid student */
        @Test
        @DisplayName("Valid student passes validation")
        public void validStudentPassesValidation() {
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(teacherDAO.findById(teacher.getId())).thenReturn(Optional.of(teacher));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            BragLog result = bragLogService.submitBragLog(bragLogRequest);
            assertThat(result).isNotNull();
        }

        /** Test invalid student */
        @Test
        @DisplayName("Invalid student fails validation")
        public void invalidStudentFailsValidation() {
            when(studentDAO.findById(student.getId())).thenReturn(Optional.empty());
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
                when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
                when(teacherDAO.findById(teacher.getId())).thenReturn(Optional.empty());
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        bragLogService.submitBragLog(bragLogRequest)
                );
                assertEquals("Invalid teacher ID", exception.getMessage());
            }

            /** Test invalid student - teacher relationship */
            @Test
            @DisplayName("Invalid student - teacher relationship fails validation")
            public void invalidTeacherRelationshipFailsValidation() {
                Teacher otherTeacher = createValidTeacher(2L, GradeLevel.SECOND);
                when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
                when(teacherDAO.findById(teacher.getId())).thenReturn(Optional.of(otherTeacher));
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
                bragLogRequest = createBragLogRequest(student, teacher, Set.of());
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        bragLogService.submitBragLog(bragLogRequest)
                );
                assertEquals("At least one behavior must be selected", exception.getMessage());
            }

            /** Test invalid behaviors */
            @Test
            @DisplayName("Invalid behaviors fail validation")
            public void invalidBehaviorsFailValidation() {
                when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
                when(teacherDAO.findById(teacher.getId())).thenReturn(Optional.of(teacher));
                lenient().when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
                lenient().when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.empty());
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        bragLogService.submitBragLog(bragLogRequest)
                );
                String message = exception.getMessage();
                assertTrue(message.equals("Invalid behavior ID: " + behaviorType1.getId())
                        || message.equals("Invalid behavior ID: " + behaviorType2.getId()));
            }
        }
    }
}
