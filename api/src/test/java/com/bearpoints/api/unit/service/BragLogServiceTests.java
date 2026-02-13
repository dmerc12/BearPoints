package com.bearpoints.api.unit.service;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.BragLogDTO;
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
 * @version 3.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class BragLogServiceTests {
    @Mock
    private BragLogDAO bragLogDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private BehaviorTypeDAO behaviorTypeDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private BragLogServiceImpl bragLogService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Teacher teacher;
    private Student student;
    private BehaviorType behaviorType1;
    private BehaviorType behaviorType2;
    private User adminUser;
    private User teacherUser;
    private User studentUser;
    private BragLog bragLog;
    private BragLogDTO bragLogDTO;

    @BeforeEach
    public void setup() {
        teacher = createValidTeacher(1L, GradeLevel.FIRST);
        student = createValidStudent(1L, teacher);
        behaviorType1 = createValidBehaviorType(1L, "Helping Others", 2);
        behaviorType2 = createValidBehaviorType(2L, "Participated", 5);
        adminUser = createValidUser(100L, Role.ADMIN);
        teacherUser = createValidUser(101L, Role.TEACHER);
        studentUser = createValidUser(102L, Role.STUDENT);
        bragLog = createValidBragLog(student, Set.of(behaviorType1, behaviorType2));
        bragLog.setSubmitterName("John Doe");
        bragLog.setSubmitterUser(adminUser);
        bragLogDTO = new BragLogDTO(1L, student.getId(), teacher.getId(),
                Set.of(behaviorType1.getId(), behaviorType2.getId()), "test notes 1",
                "John Doe", "ValidFirstName", "ValidLastName", GradeLevel.FIRST,
                null, 7, null);
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
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching brag logs")
    class WhenSearchingBragLogs {
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
    @SuppressWarnings("unchecked")
    @DisplayName("When creating brag log")
    class WhenCreatingBragLog {
        private final String validSubmitterName = "Jane Smith";
        private final String[] nameParts = validSubmitterName.split("\\s+");
        private final String firstName = nameParts[0];
        private final String lastName = validSubmitterName.substring(firstName.length()).trim();

        @Test
        @DisplayName("Should create new brag log with submitter name only (no matching user)")
        void shouldCreateNewBragLogWithSubmitterNameOnly() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), validSubmitterName, null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.empty());
            BragLog savedBragLog = createValidBragLog(student, Set.of(behaviorType1, behaviorType2));
            savedBragLog.setSubmitterName(validSubmitterName);
            savedBragLog.setSubmitterUser(null);
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(savedBragLog);
            BragLogDTO result = bragLogService.createBragLog(createDTO);
            assertNotNull(result);
            assertEquals(bragLog.getId(), result.getId());
            assertEquals(validSubmitterName, result.getSubmitterName());
            assertNull(result.getSubmitterUserId());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should create new brag log and link existing ADMIN user")
        void shouldCreateNewBragLogAndLinkAdminUser() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            String submitterName = "ValidFirstName ValidLastName";
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), submitterName, null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(adminUser));
            BragLog savedBragLog = createValidBragLog(student, Set.of(behaviorType1, behaviorType2));
            savedBragLog.setSubmitterName(submitterName);
            savedBragLog.setSubmitterUser(adminUser);
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(savedBragLog);
            BragLogDTO result = bragLogService.createBragLog(createDTO);
            assertNotNull(result);
            assertEquals(bragLog.getId(), result.getId());
            assertEquals(submitterName, result.getSubmitterName());
            assertEquals(adminUser.getId(), result.getSubmitterUserId());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should create new brag log and link existing TEACHER user")
        void shouldCreateNewBragLogAndLinkTeacherUser() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            String submitterName = "ValidFirstName ValidLastName";
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), submitterName, null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(teacherUser));
            BragLog savedBragLog = createValidBragLog(student, Set.of(behaviorType1, behaviorType2));
            savedBragLog.setSubmitterName(submitterName);
            savedBragLog.setSubmitterUser(teacherUser);
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(savedBragLog);
            BragLogDTO result = bragLogService.createBragLog(createDTO);
            assertNotNull(result);
            assertEquals(bragLog.getId(), result.getId());
            assertEquals(submitterName, result.getSubmitterName());
            assertEquals(teacherUser.getId(), result.getSubmitterUserId());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when submitter name matches a STUDENT")
        void shouldThrowWhenSubmitterIsStudent() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            String submitterName = "ValidFirstName ValidLastName";
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), submitterName, null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(studentUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.createBragLog(createDTO)
            );
            assertEquals("Students cannot submit brag logs", ex.getMessage());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when submitter name has no space")
        void shouldThrowWhenSubmitterNameHasNoSpace() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), "SingleName", null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.createBragLog(createDTO)
            );
            assertEquals("Submitter name must contain both first and last name", ex.getMessage());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when submitter name is blank")
        void shouldThrowWhenSubmitterNameIsBlank() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null, behaviorIds,
                    bragLog.getNotes(), "", null, null,
                    null, null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.createBragLog(createDTO)
            );
            assertEquals("Submitter name must not be blank", ex.getMessage());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when submitter name is null")
        void shouldThrowWhenSubmitterNameIsNull() {
            Set<Long> behaviorIds = Set.of(behaviorType1.getId(), behaviorType2.getId());
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null, behaviorIds,
                    bragLog.getNotes(), null, null, null,
                    null, null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorType1.getId())).thenReturn(Optional.of(behaviorType1));
            when(behaviorTypeDAO.findById(behaviorType2.getId())).thenReturn(Optional.of(behaviorType2));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.createBragLog(createDTO)
            );
            assertEquals("Submitter name must not be blank", ex.getMessage());
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO, times(2)).findById(anyLong());
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentNotFound() {
            Long studentId = 9999L;
            Set<Long> behaviorIds = bragLogDTO.getBehaviorIds();
            BragLogDTO createDTO = new BragLogDTO(null, studentId, null,
                    behaviorIds, bragLog.getNotes(), "John Doe", null, null, null,
                    null, null, null);
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.createBragLog(createDTO));
            verify(studentDAO).findById(studentId);
            verify(behaviorTypeDAO, never()).findById(anyLong());
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when behavior type not found")
        void shouldThrowResourceNotFoundExceptionWhenBehaviorTypeNotFound() {
            Long behaviorTypeId = 9999L;
            Set<Long> behaviorIds = Set.of(behaviorTypeId);
            BragLogDTO createDTO = new BragLogDTO(null, student.getId(), null,
                    behaviorIds, bragLog.getNotes(), "John Doe", null, null, null,
                    null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogService.createBragLog(createDTO));
            verify(studentDAO).findById(student.getId());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When updating brag log")
    class WhenUpdatingBragLog {
        @Test
        @DisplayName("Should update existing brag log successfully")
        void shouldUpdateExistingBragLogSuccessfully() {
            Long bragLogId = bragLog.getId();
            String updatedNotes = "Updated notes";
            Set<Long> updatedBehaviorIds = Set.of(1L);
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    updatedBehaviorIds, updatedNotes, "John Doe", null, null,
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
                    bragLogDTO.getBehaviorIds(), bragLogDTO.getNotes(), "John Doe", null, null, null,
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
                    bragLogDTO.getBehaviorIds(), newNotes, "John Doe", null, null, null,
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
        @DisplayName("Should update submitter name and re-resolve to ADMIN user")
        void shouldUpdateSubmitterNameAndReResolveToAdminUser() {
            Long bragLogId = bragLog.getId();
            String newName = "Jane Admin";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), newName, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(adminUser));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            bragLogService.updateBragLog(bragLogId, updateDTO);
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(argThat(log ->
                    log.getSubmitterName().equals(newName) &&
                    log.getSubmitterUser() != null &&
                    log.getSubmitterUser().getId().equals(adminUser.getId())
            ));
        }

        @Test
        @DisplayName("Should update submitter name and re-resolve to TEACHER user")
        void shouldUpdateSubmitterNameAndReResolveToTeacherUser() {
            Long bragLogId = bragLog.getId();
            String newName = "Jane Teacher";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), newName, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(teacherUser));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            bragLogService.updateBragLog(bragLogId, updateDTO);
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(argThat(log ->
                    log.getSubmitterName().equals(newName) &&
                    log.getSubmitterUser() != null &&
                    log.getSubmitterUser().getId().equals(teacherUser.getId())
            ));
        }

        @Test
        @DisplayName("Should clear submitter user when updated name matches no user")
        void shouldClearSubmitterUserWhenUpdatedNameMatchesNoUser() {
            Long bragLogId = bragLog.getId();
            String newName = "Unknown Person";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), newName, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.empty());
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            bragLogService.updateBragLog(bragLogId, updateDTO);
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO).save(argThat(log ->
                    log.getSubmitterName().equals(newName) &&
                    log.getSubmitterUser() == null
            ));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updated submitter name matches a STUDENT")
        void shouldThrowIllegalArgumentExceptionWhenUpdatedSubmitterNameMatchesAStudent() {
            Long bragLogId = bragLog.getId();
            String newName = "ValidFirstName ValidLastName";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), newName, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(userDAO.findOne(any(Specification.class))).thenReturn(Optional.of(studentUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.updateBragLog(bragLogId, updateDTO)
            );
            assertEquals("Students cannot submit brag logs", ex.getMessage());
            verify(bragLogDAO).findById(bragLogId);
            verify(userDAO).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updated submitter name has no space")
        void shouldThrowIllegalArgumentExceptionWhenUpdatedSubmitterNameHasNoSpace() {
            Long bragLogId = bragLog.getId();
            String newName = "SingleName";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), newName, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.updateBragLog(bragLogId, updateDTO)
            );
            assertEquals("Submitter name must contain both first and last name", ex.getMessage());
            verify(bragLogDAO).findById(bragLogId);
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updated submitter name is blank")
        void shouldThrowIllegalArgumentExceptionWhenUpdatedSubmitterNameIsBlank() {
            Long bragLogId = bragLog.getId();
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), "", null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.updateBragLog(bragLogId, updateDTO)
            );
            assertEquals("Submitter name must not be blank", ex.getMessage());
            verify(bragLogDAO).findById(bragLogId);
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updated submitter name is null")
        void shouldThrowIllegalArgumentExceptionWhenUpdatedSubmitterNameIsNull() {
            Long bragLogId = bragLog.getId();
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), null, null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bragLogService.updateBragLog(bragLogId, updateDTO)
            );
            assertEquals("Submitter name must not be blank", ex.getMessage());
            verify(bragLogDAO).findById(bragLogId);
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO, never()).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should not re-resolve user if submitter name unchanged")
        void shouldNotReResolveUserIfSubmitterNameUnchanged() {
            Long bragLogId = bragLog.getId();
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(),
                    bragLogDTO.getBehaviorIds(), bragLog.getNotes(), bragLog.getSubmitterName(), null,
                    null, null, null, null, null);
            when(bragLogDAO.findById(bragLogId)).thenReturn(Optional.of(bragLog));
            when(bragLogDAO.save(any(BragLog.class))).thenReturn(bragLog);
            bragLogService.updateBragLog(bragLogId, updateDTO);
            verify(userDAO, never()).findOne(any(Specification.class));
            verify(bragLogDAO).save(any(BragLog.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when brag log not found by ID")
        void shouldThrowResourceNotFoundExceptionWhenBragLogNotFoundById() {
            Long bragLogId = 999L;
            Set<Long> newBehaviorIds = Set.of(behaviorType1.getId());
            String newNotes = "updated notes";
            BragLogDTO updateDTO = new BragLogDTO(bragLogId, student.getId(), teacher.getId(), newBehaviorIds,
                    newNotes, "John Doe", null, null, null, null,
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
}
