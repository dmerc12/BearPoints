package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.UserNotFoundException;
import com.bearpoints.api.service.impl.TeacherServiceImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherServiceImpl}.
 * <p>Verifies teacher user management functionality including CRUD operations and search.
 *
 * @see TeacherServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("TeacherService Tests")
@ExtendWith(MockitoExtension.class)
public class TeacherServiceTests {
    @Mock
    private UserDAO userDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When retrieving teachers")
    class WhenRetrievingTeachersTests {
        @Test
        @DisplayName("Should retrieve all teachers with pagination")
        void shouldRetrieveAllTeachersWithPagination() {
            List<Teacher> teachers = List.of(
                    createTeacher(1L, 1L, "teacher1@okcps.org", "Teacher1", "User1", Role.TEACHER, GradeLevel.FIRST),
                    createTeacher(2L, 2L, "teacher2@okcps.org", "Teacher2", "User2", Role.TEACHER, GradeLevel.SECOND)
            );
            Page<Teacher> teacherPage = new PageImpl<>(teachers, pageable, 2L);
            when(teacherDAO.findAll(any(Pageable.class))).thenReturn(teacherPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.getAllTeachers(pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(2L, result.getTotalElements());
            verify(teacherDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no teachers exist")
        void shouldReturnEmptyPageWhenNoTeachersExist() {
            Page<Teacher> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(teacherDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.getAllTeachers(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("When searching teachers")
    class WhenSearchingTeachersTests {
        @Test
        @DisplayName("Should search teachers by email")
        void shouldSearchTeachersByEmail() {
            String searchTerm = "teacher";
            String email = searchTerm + "1@okcps.org";
            List<Teacher> teachers = List.of(
                    createTeacher(1L, 1L, email, "Teacher1", "User1", Role.TEACHER, GradeLevel.FIRST)
            );
            Page<Teacher> teacherPage = new PageImpl<>(teachers, pageable, 1L);
            when(teacherDAO.findByUserEmailContainingIgnoreCase(eq(searchTerm), any(Pageable.class)))
                    .thenReturn(teacherPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.searchTeachersByEmail(searchTerm, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(email, result.getContent().getFirst().getUser().getEmail());
            verify(teacherDAO).findByUserEmailContainingIgnoreCase(searchTerm, pageable);
        }

        @Test
        @DisplayName("Should search teachers by first name")
        void shouldSearchTeachersByFirstName() {
            String firstName = "John";
            List<Teacher> teachers = List.of(
                    createTeacher(1L, 1L, "john@okcps.org", firstName, "Doe", Role.TEACHER, GradeLevel.FIRST)
            );
            Page<Teacher> teacherPage = new PageImpl<>(teachers, pageable, 1L);
            when(teacherDAO.findByUserFirstNameContainingIgnoreCase(eq(firstName), any(Pageable.class)))
                    .thenReturn(teacherPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.searchTeachersByFirstName(firstName, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(firstName, result.getContent().getFirst().getUser().getFirstName());
            verify(teacherDAO).findByUserFirstNameContainingIgnoreCase(firstName, pageable);
        }

        @Test
        @DisplayName("Should search teachers by last name")
        void shouldSearchTeachersByLastName() {
            String lastName = "Doe";
            List<Teacher> teachers = List.of(
                    createTeacher(1L, 1L, "john@okcps.org", "John", lastName, Role.TEACHER, GradeLevel.FIRST)
            );
            Page<Teacher> teacherPage = new PageImpl<>(teachers, pageable, 1L);
            when(teacherDAO.findByUserLastNameContainingIgnoreCase(eq(lastName), any(Pageable.class)))
                    .thenReturn(teacherPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.searchTeachersByLastName(lastName, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(lastName, result.getContent().getFirst().getUser().getLastName());
            verify(teacherDAO).findByUserLastNameContainingIgnoreCase(lastName, pageable);
        }

        @Test
        @DisplayName("Should search teachers by grade level")
        void shouldSearchTeachersByGradesLevel() {
            GradeLevel gradeLevel = GradeLevel.FIRST;
            List<Teacher> teachers = List.of(
                    createTeacher(1L, 1L, "john@okcps.org", "John", "Doe", Role.TEACHER, gradeLevel)
            );
            Page<Teacher> teacherPage = new PageImpl<>(teachers, pageable, 1L);
            when(teacherDAO.findByGrade(eq(gradeLevel), any(Pageable.class)))
                    .thenReturn(teacherPage);
            PagedResponseDTO<TeacherDTO> result = teacherService.searchTeachersByGrade(gradeLevel, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(gradeLevel, result.getContent().getFirst().getGrade());
            verify(teacherDAO).findByGrade(gradeLevel, pageable);
        }
    }

    @Nested
    @DisplayName("When retrieving teacher by ID")
    class WhenRetrievingTeacherByIdTests {
        @Test
        @DisplayName("Should return teacher when found")
        void shouldReturnTeacherWhenFound() {
            Long teacherId = 1L;
            String email = "teacher@okcps.org";
            String firstName = "John";
            String lastName = "Doe";
            Role role = Role.TEACHER;
            GradeLevel gradeLevel = GradeLevel.FIRST;
            Teacher teacher = createTeacher(teacherId, 1L, email, firstName, lastName, role, gradeLevel);
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(teacher));
            TeacherDTO result = teacherService.getTeacherById(teacherId);
            assertNotNull(result);
            assertEquals(teacherId, result.getId());
            assertEquals(email, result.getUser().getEmail());
            assertEquals(firstName, result.getUser().getFirstName());
            assertEquals(lastName, result.getUser().getLastName());
            assertEquals(role, result.getUser().getRole());
            assertEquals(gradeLevel, result.getGrade());
            verify(teacherDAO).findById(teacherId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void shouldThrowUserNotFoundExceptionWhenNotFound() {
            Long teacherId = 999L;
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> teacherService.getTeacherById(teacherId));
        }
    }

    @Nested
    @DisplayName("When creating teacher")
    class WhenCreatingTeacherTests {
        @Test
        @DisplayName("Should create new teacher successfully")
        void shouldCreateNewTeacherSuccessfully() {
            String email = "new.teacher@okcps.org";
            GradeLevel gradeLevel = GradeLevel.FIRST;
            Role role = Role.TEACHER;
            TeacherDTO teacherDTO = new TeacherDTO(
                    null,
                    new UserDTO(null, email, "New", "Teacher", "TEACHER"),
                    "FIRST"
            );
            when(teacherDAO.findByUserEmail(email)).thenReturn(Optional.empty());
            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setEmail(email);
            savedUser.setFirstName("New");
            savedUser.setLastName("Teacher");
            savedUser.setRole(role);
            Teacher savedTeacher = createTeacher(1L, 1L, email, "New", "Teacher", role, gradeLevel);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(savedTeacher);
            TeacherDTO result = teacherService.createTeacher(teacherDTO);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(email, result.getUser().getEmail());
            assertEquals(gradeLevel, result.getGrade());
            assertEquals(role, result.getUser().getRole());
            verify(teacherDAO).findByUserEmail(email);
            verify(userDAO).save(any(User.class));
            verify(teacherDAO).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowDuplicateResourceExceptionWhenEmailAlreadyExists() {
            String existingEmail = "existing@okcps.org";
            TeacherDTO teacherDTO = new TeacherDTO(
                    null,
                    new UserDTO(null, existingEmail, "New", "Teacher", "TEACHER"),
                    "FIRST"
            );
            Teacher existingTeacher = createTeacher(1L, 1L, existingEmail, "Existing", "User", Role.TEACHER, GradeLevel.FIRST);
            when(teacherDAO.findByUserEmail(existingEmail)).thenReturn(Optional.of(existingTeacher));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.createTeacher(teacherDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(teacherDAO).findByUserEmail(existingEmail);
            verify(teacherDAO, never()).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should create teacher with TEACHER role regardless of input role")
        void shouldCreateTeacherWithTeacherRoleRegardlessOfInputRole() {
            String newEmail = "new@okcps.org";
            String firstName = "John";
            String lastName = "Doe";
            Role role = Role.TEACHER;
            GradeLevel gradeLevel = GradeLevel.FIRST;
            TeacherDTO teacherDTO = new TeacherDTO(
                    null,
                    new UserDTO(null, newEmail, firstName, lastName, "STUDENT"),
                    "FIRST"
            );
            when(teacherDAO.findByUserEmail(newEmail)).thenReturn(Optional.empty());
            Teacher savedTeacher = createTeacher(1L, 1L, newEmail, firstName, lastName, role, gradeLevel);
            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setEmail(newEmail);
            savedUser.setFirstName("New");
            savedUser.setLastName("Teacher");
            savedUser.setRole(role);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(savedTeacher);
            TeacherDTO result = teacherService.createTeacher(teacherDTO);
            assertEquals(Role.TEACHER, result.getUser().getRole());
            verify(userDAO).save(any(User.class));
            verify(teacherDAO).save(argThat(teacher -> teacher.getUser().getRole() == Role.TEACHER));
        }
    }

    @Nested
    @DisplayName("When updating teacher")
    class WhenUpdatingTeacherTests {
        @Test
        @DisplayName("Should update existing teacher successfully")
        void shouldUpdateExistingTeacherSuccessfully() {
            Long teacherId = 1L;
            Role role = Role.TEACHER;
            String newEmail = "new@okcps.org";
            String newFirstName = "New";
            String newLastName = "Last-Name";
            GradeLevel newGrade = GradeLevel.SECOND;
            Teacher existingTeacher = createTeacher(teacherId, 1L, "old@okcps.org", "Old", "Name", role, GradeLevel.FIRST);
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, newEmail, newFirstName, newLastName, "TEACHER"),
                    "SECOND"
            );
            when(teacherDAO.findByUserEmail(newEmail)).thenReturn(Optional.empty());
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(existingTeacher));
            Teacher updatedTeacher = createTeacher(teacherId, 1L, newEmail, newFirstName, newLastName, role, newGrade);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(updatedTeacher);
            TeacherDTO result = teacherService.updateTeacher(teacherId, updateDTO);
            assertNotNull(result);
            assertEquals(newEmail, result.getUser().getEmail());
            assertEquals(newFirstName, result.getUser().getFirstName());
            assertEquals(newLastName, result.getUser().getLastName());
            assertEquals(newGrade, result.getGrade());
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO).findByUserEmail(newEmail);
            verify(teacherDAO).save(argThat(teacher ->
                    teacher.getUser().getEmail().equals(newEmail) &&
                    teacher.getGrade() == newGrade
            ));
        }

        @Test
        @DisplayName("Should update teacher without checking email when email unchanged")
        void shouldUpdateTeacherWithoutCheckingEmailWhenEmailUnchanged() {
            Long teacherId = 1L;
            String sameEmail = "same@okcps.org";
            String newFirstName = "New";
            String newLastName = "Last-Name";
            Role role = Role.TEACHER;
            GradeLevel newGrade = GradeLevel.SECOND;
            Teacher existingTeacher = createTeacher(teacherId, 1L, sameEmail, "Old", "Name", role, GradeLevel.FIRST);
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, sameEmail, newFirstName, newLastName, "TEACHER"),
                    "SECOND"
            );
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(existingTeacher));
            Teacher updatedTeacher = createTeacher(teacherId, 1L, sameEmail, newFirstName, newLastName, role, newGrade);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(updatedTeacher);
            TeacherDTO result = teacherService.updateTeacher(teacherId, updateDTO);
            assertNotNull(result);
            assertEquals(sameEmail, result.getUser().getEmail());
            assertEquals(newFirstName, result.getUser().getFirstName());
            assertEquals(newLastName, result.getUser().getLastName());
            assertEquals(newGrade, result.getGrade());
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO, never()).findByUserEmail(anyString());
            verify(teacherDAO).save(argThat(teacher ->
                    teacher.getUser().getFirstName().equals(newFirstName) &&
                    teacher.getGrade() == newGrade
            ));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing email")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail() {
            Long teacherId = 1L;
            String existingEmail = "existing@okcps.org";
            Role role = Role.TEACHER;
            Teacher existingTeacher = createTeacher(teacherId, 1L, "old@okcps.org", "Old", "Name", role, GradeLevel.FIRST);
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, existingEmail, "New", "Last-Name", "TEACHER"),
                    "SECOND"
            );
            Teacher otherTeacher = createTeacher(2L, 2L, existingEmail, "Other", "User", role, GradeLevel.THIRD);
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(existingTeacher));
            when(teacherDAO.findByUserEmail(existingEmail)).thenReturn(Optional.of(otherTeacher));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> teacherService.updateTeacher(teacherId, updateDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO).findByUserEmail(existingEmail);
            verify(teacherDAO, never()).save(any(Teacher.class));
        }

        @Test
        @DisplayName("Should allow update when email exists but is the same teacher")
        void shouldAllowUpdateWhenEmailExistsButIsSameTeacher() {
            Long teacherId = 1L;
            String sameEmail = "same@okcps.org";
            String newFirstName = "New";
            String newLastName = "Last-Name";
            Role role = Role.TEACHER;
            GradeLevel newGrade = GradeLevel.SECOND;
            Teacher existingTeacher = createTeacher(teacherId, 1L, sameEmail, "Old", "Name", role, GradeLevel.FIRST);
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, sameEmail, newFirstName, newLastName, "TEACHER"),
                    "SECOND"
            );
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(existingTeacher));
            Teacher updatedTeacher = createTeacher(teacherId, 1L, sameEmail, newFirstName, newLastName, role, newGrade);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(updatedTeacher);
            TeacherDTO result = teacherService.updateTeacher(teacherId, updateDTO);
            assertNotNull(result);
            assertEquals(sameEmail, result.getUser().getEmail());
            assertEquals(newFirstName, result.getUser().getFirstName());
            assertEquals(newLastName, result.getUser().getLastName());
            assertEquals(newGrade, result.getGrade());
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO, never()).findByUserEmail(anyString());
            verify(teacherDAO).save(argThat(teacher ->
                    teacher.getUser().getFirstName().equals(newFirstName) &&
                    teacher.getGrade() == newGrade
            ));
        }

        @Test
        @DisplayName("Should allow update when email exists but is same teacher (different ID scenario)")
        void shouldAllowUpdateWhenEmailExistsButIsSameTeacherDifferentID() {
            Long teacherId = 1L;
            String newEmail = "new@okcps.org";
            String newFirstName = "New";
            String newLastName = "Last-Name";
            Role role = Role.TEACHER;
            GradeLevel newGrade = GradeLevel.SECOND;
            Teacher existingTeacher = createTeacher(teacherId, 1L, "old@okcps.org", "Old", "Name", role, GradeLevel.FIRST);
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, newEmail, newFirstName, newLastName, "TEACHER"),
                    "SECOND"
            );
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(existingTeacher));
            when(teacherDAO.findByUserEmail(newEmail)).thenReturn(Optional.of(existingTeacher));
            Teacher updatedTeacher = createTeacher(teacherId, 1L, newEmail, newFirstName, newLastName, role, newGrade);
            when(teacherDAO.save(any(Teacher.class))).thenReturn(updatedTeacher);
            TeacherDTO result = teacherService.updateTeacher(teacherId, updateDTO);
            assertNotNull(result);
            assertEquals(newEmail, result.getUser().getEmail());
            assertEquals(newFirstName, result.getUser().getFirstName());
            assertEquals(newLastName, result.getUser().getLastName());
            assertEquals(newGrade, result.getGrade());
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO).findByUserEmail(newEmail);
            verify(teacherDAO).save(argThat(teacher ->
                    teacher.getUser().getFirstName().equals(newFirstName) &&
                    teacher.getGrade() == newGrade
            ));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void shouldThrowUserNotFoundExceptionWhenTeacherNotFound() {
            Long teacherId = 999L;
            TeacherDTO updateDTO = new TeacherDTO(
                    teacherId,
                    new UserDTO(1L, "new@okcps.org", "New", "Last-Name", "TEACHER"),
                    "SECOND"
            );
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.empty());
            assertThrows(
                    UserNotFoundException.class,
                    () -> teacherService.updateTeacher(teacherId, updateDTO)
            );
        }
    }

    @Nested
    @DisplayName("When deleting teacher")
    class WhenDeletingTeacherTests {
        @Test
        @DisplayName("Should delete teacher and associated user successfully")
        void shouldDeleteTeacherAndAssociatedUserSuccessfully() {
            Long teacherId = 1L;
            Teacher teacher = createTeacher(teacherId, 1L, "teacher@okcps.org", "Teacher", "User", Role.TEACHER, GradeLevel.FIRST);
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(teacher));
            teacherService.deleteTeacher(teacherId);
            verify(teacherDAO).findById(teacherId);
            verify(teacherDAO).delete(teacher);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void shouldThrowUserNotFoundExceptionWhenTeacherNotFound() {
            Long teacherId = 999L;
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> teacherService.deleteTeacher(teacherId));
        }
    }

    private Teacher createTeacher(Long id, Long userId, String email, String firstName, String lastName, Role role, GradeLevel grade) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        teacher.setUser(user);
        teacher.setGrade(grade);
        return teacher;
    }
}
