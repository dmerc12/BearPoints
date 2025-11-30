package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.*;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.UserNotFoundException;
import com.bearpoints.api.service.impl.StudentServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentServiceImpl}.
 * <p>Verifies student management functionality including CRUD operations,
 * search with criteria, and classroom leaderboards.
 *
 * @see StudentServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("StudentService Tests")
@ExtendWith(MockitoExtension.class)
public class StudentServiceTests {
    @Mock
    private StudentDAO studentDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private StudentServiceImpl studentService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When retrieving all students")
    class WhenRetrievingAllStudents {
        @Test
        @DisplayName("Should retrieve all students with pagination")
        void shouldRetrieveAllStudentsWithPagination() {
            List<Student> students = List.of(
                    createStudent(1L, 100),
                    createStudent(2L, 150)
            );
            Page<Student> studentPage = new PageImpl<>(students, pageable, 2L);
            when(studentDAO.findAll(any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.getAllStudents(pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(2L, result.getTotalElements());
            verify(studentDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no student exists")
        void shouldReturnEmptyPageWhenNoStudentsExist() {
            Page<Student> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(studentDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<StudentDTO> result = studentService.getAllStudents(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching students with criteria")
    class WhenSearchingStudentsWithCriteria {
        @Test
        @DisplayName("Should search students with email criteria")
        void shouldSearchStudentsWithEmailCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setEmail("test@okcps.org");
            List<Student> students = List.of(createStudent(1L, 100));
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search students with first name criteria")
        void shouldSearchStudentsWithFirstNameCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setFirstName("John");
            List<Student> students = List.of(createStudent(1L, 100));
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search students with last name criteria")
        void shouldSearchStudentsWithLastNameCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setLastName("Doe");
            List<Student> students = List.of(createStudent(1L, 100));
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search students with teacher criteria")
        void shouldSearchStudentsWithTeacherCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setTeacherId(1L);
            List<Student> students = List.of(createStudent(1L, 100));
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search students with points range criteria")
        void shouldSearchStudentsWithPointsRangeCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setMinPoints(50);
            criteria.setMaxPoints(150);
            List<Student> students = List.of(createStudent(1L, 100));
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all students with no criteria specified")
        void shouldReturnAllStudentsWhenNoCriteriaSpecified() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            List<Student> students = List.of(
                    createStudent(1L, 100),
                    createStudent(2L, 150)
            );
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1L);
            when(studentDAO.findAll(any(Pageable.class))).thenReturn(studentPage);
            PagedResponseDTO<StudentDTO> result = studentService.searchStudents(criteria, pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(studentDAO).findAll(pageable);
            verify(studentDAO, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving classroom leaderboard")
    class WhenRetrievingClassroomLeaderboard {
        @Test
        @DisplayName("Should retrieve classroom leaderboard successfully")
        void shouldRetrieveClassroomLeaderboardSuccessfully() {
            Long teacherId = 1L;
            Teacher teacher = createTeacher(teacherId, GradeLevel.SECOND);
            List<Student> students = List.of(
                    createStudent(1L, 200),
                    createStudent(2L, 150)
            );
            Page<Student> leaderboardPage = new PageImpl<>(students, pageable, 2L);
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.of(teacher));
            when(studentDAO.findByTeacherOrderByPointsDesc(eq(teacher), any(Pageable.class))).thenReturn(leaderboardPage);
            PagedResponseDTO<StudentDTO> result = studentService.getClassRoomLeaderboard(teacherId, pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(200, result.getContent().getFirst().getPoints());
            verify(teacherDAO).findById(teacherId);
            verify(studentDAO).findByTeacherOrderByPointsDesc(teacher, pageable);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found for leaderboard")
        void shouldThrowUserNotFoundExceptionWhenTeacherNotFoundForLeaderboard() {
            Long teacherId = 999L;
            when(teacherDAO.findById(teacherId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class,
                    () ->  studentService.getClassRoomLeaderboard(teacherId, pageable));
            verify(teacherDAO).findById(teacherId);
            verify(studentDAO, never()).findByTeacherOrderByPointsDesc(any(Teacher.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving student by identifier")
    class WhenRetrievingStudentByIdentifier {
        @Test
        @DisplayName("Should return student by ID when found")
        void shouldReturnStudentByIdWhenFound() {
            Long studentId = 1L;
            Student student = createStudent(studentId, 100);
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            StudentDTO result = studentService.getStudentById(studentId);
            assertNotNull(result);
            assertEquals(studentId, result.getId());
            assertEquals(100, result.getPoints());
            verify(studentDAO).findById(studentId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when student not found by ID")
        void shouldThrowUserNotFoundExceptionWhenStudentNotFoundById() {
            Long studentId = 999L;
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class,
                    () ->  studentService.getStudentById(studentId));
            verify(studentDAO).findById(studentId);
        }

        @Test
        @DisplayName("Should return student by token when found")
        void shouldReturnStudentByTokenWhenFound() {
            Student student = createStudent(1L, 100);
            String token = student.getToken();
            when(studentDAO.findByToken(token)).thenReturn(Optional.of(student));
            StudentDTO result = studentService.getStudentByToken(token);
            assertNotNull(result);
            assertEquals(token, result.getToken());
            verify(studentDAO).findByToken(token);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when student not found by token")
        void shouldThrowUserNotFoundExceptionWhenStudentNotFoundByToken() {
            String token = "invalid-token";
            when(studentDAO.findByToken(token)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class,
                    () ->  studentService.getStudentByToken(token));
            verify(studentDAO).findByToken(token);
        }
    }

    @Nested
    @DisplayName("When creating student")
    class WhenCreatingStudent {
        @Test
        @DisplayName("Should create new student successfully")
        void shouldCreateNewStudentSuccessfully() {
            StudentDTO studentDTO = new StudentDTO(
                    null,
                    new UserDTO(null, "new.student@okcps.org", "New", "Student", "STUDENT"),
                    0,
                    null,
                    new TeacherDTO(1L, null, "THIRD")
            );
            Teacher teacher = createTeacher(1L, GradeLevel.THIRD);
            User savedUser = createUser(1L, studentDTO.getUser().getEmail(), studentDTO.getUser().getFirstName(), studentDTO.getUser().getLastName(), Role.STUDENT);
            Student savedStudent = createStudent(1L, 0);
            savedStudent.setUser(savedUser);
            savedStudent.setTeacher(teacher);
            when(studentDAO.findByUserEmail(studentDTO.getUser().getEmail())).thenReturn(Optional.empty());
            when(teacherDAO.findById(1L)).thenReturn(Optional.of(teacher));
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            when(studentDAO.save(any(Student.class))).thenReturn(savedStudent);
            StudentDTO result = studentService.createStudent(studentDTO);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(studentDTO.getUser().getEmail(), result.getUser().getEmail());
            assertEquals(Role.STUDENT, result.getUser().getRole());
            verify(studentDAO).findByUserEmail(studentDTO.getUser().getEmail());
            verify(teacherDAO).findById(teacher.getId());
            verify(userDAO).save(any(User.class));
            verify(studentDAO).save(any(Student.class));

        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowDuplicateResourceExceptionWhenEmailAlreadyExists() {
            StudentDTO studentDTO = new StudentDTO(
                    null,
                    new UserDTO(null, "existing@okcps.org", "New", "Student", "STUDENT"),
                    0,
                    null,
                    new TeacherDTO(1L, null, "FIRST")
            );
            Student existingStudent = createStudent(1L, 100);
            when(studentDAO.findByUserEmail(studentDTO.getUser().getEmail())).thenReturn(Optional.of(existingStudent));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.createStudent(studentDTO)
            );
            assertEquals("A student with this email already exists", exception.getMessage());
            verify(studentDAO).findByUserEmail(studentDTO.getUser().getEmail());
            verify(studentDAO, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void shouldThrowUserNotFoundExceptionWhenTeacherNotFound() {
            StudentDTO studentDTO = new StudentDTO(
                    null,
                    new UserDTO(null, "new@okcps.org", "New", "Student", "STUDENT"),
                    0,
                    null,
                    new TeacherDTO(999L, null, "THIRD")
            );
            when(studentDAO.findByUserEmail(studentDTO.getUser().getEmail())).thenReturn(Optional.empty());
            when(teacherDAO.findById(studentDTO.getTeacher().getId())).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> studentService.createStudent(studentDTO));
            verify(studentDAO).findByUserEmail(studentDTO.getUser().getEmail());
            verify(teacherDAO).findById(studentDTO.getTeacher().getId());
            verify(studentDAO, never()).save(any(Student.class));
        }
    }

    @Nested
    @DisplayName("When updating student")
    class WhenUpdatingStudent {
        @Test
        @DisplayName("Should update existing student successfully")
        void shouldUpdateExistingStudentSuccessfully() {
            Long studentId = 1L;
            Student existingStudent = createStudent(studentId, 100);
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(existingStudent.getUser().getId(), "updated@okcps.org", "Updated", "Student", "STUDENT"),
                    150,
                    existingStudent.getToken(),
                    new TeacherDTO(2L, null, "THIRD")
            );
            Teacher newTeacher = createTeacher(2L, GradeLevel.THIRD);
            Student updatedStudent = createStudent(studentId, 150);
            updatedStudent.getUser().setEmail(updateDTO.getUser().getEmail());
            updatedStudent.getUser().setFirstName(updateDTO.getUser().getFirstName());
            updatedStudent.getUser().setLastName(updateDTO.getUser().getLastName());
            updatedStudent.setTeacher(newTeacher);
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(existingStudent));
            when(studentDAO.findByUserEmail(updateDTO.getUser().getEmail())).thenReturn(Optional.empty());
            when(teacherDAO.findById(2L)).thenReturn(Optional.of(newTeacher));
            when(studentDAO.save(any(Student.class))).thenReturn(updatedStudent);
            StudentDTO result = studentService.updateStudent(studentId, updateDTO);
            assertNotNull(result);
            assertEquals(updateDTO.getUser().getEmail(), result.getUser().getEmail());
            assertEquals(updateDTO.getUser().getFirstName(), result.getUser().getFirstName());
            assertEquals(updateDTO.getUser().getLastName(), result.getUser().getLastName());
            assertEquals(updateDTO.getPoints(), result.getPoints());
            assertEquals(updateDTO.getId(), result.getId());
            assertEquals(updateDTO.getTeacher().getId(), result.getTeacher().getId());
            verify(studentDAO).findById(studentId);
            verify(studentDAO).findByUserEmail(updateDTO.getUser().getEmail());
            verify(teacherDAO).findById(2L);
            verify(studentDAO).save(existingStudent);
        }

        @Test
        @DisplayName("Should update student without checking email when email unchanged")
        void shouldUpdateStudentWithoutCheckingEmailWhenEmailUnchanged() {
            Long studentId = 1L;
            Student existingStudent = createStudent(studentId, 100);
            String sameEmail = existingStudent.getUser().getEmail();
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(existingStudent.getUser().getId(), sameEmail, "Updated", "Student", "STUDENT"),
                    150,
                    existingStudent.getToken(),
                    new TeacherDTO(1L, null, "FIRST")
            );
            Student updatedStudent = createStudent(studentId, 150);
            updatedStudent.getUser().setEmail(updateDTO.getUser().getEmail());
            updatedStudent.getUser().setFirstName(updateDTO.getUser().getFirstName());
            updatedStudent.getUser().setLastName(updateDTO.getUser().getLastName());
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(existingStudent));
            when(studentDAO.save(any(Student.class))).thenReturn(updatedStudent);
            StudentDTO result = studentService.updateStudent(studentId, updateDTO);
            assertNotNull(result);
            assertEquals(sameEmail, result.getUser().getEmail());
            assertEquals(updateDTO.getUser().getFirstName(), result.getUser().getFirstName());
            assertEquals(updateDTO.getUser().getLastName(), result.getUser().getLastName());
            assertEquals(updateDTO.getPoints(), result.getPoints());
            assertEquals(updateDTO.getId(), result.getId());
            assertEquals(updateDTO.getTeacher().getId(), result.getTeacher().getId());
            verify(studentDAO).findById(studentId);
            verify(studentDAO, never()).findByUserEmail(anyString());
            verify(teacherDAO, never()).findById(anyLong());
            verify(studentDAO).save(existingStudent);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing email")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail() {
            Long studentId = 1L;
            String existingEmail = "existing@okcps.org";
            Student existingStudent = createStudent(studentId, 100);
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(1L, existingEmail, "Updated", "Student", "STUDENT"),
                    150,
                    existingStudent.getToken(),
                    new TeacherDTO(1L, null, "FIRST")
            );
            Student otherStudent = createStudent(2L, 200);
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(existingStudent));
            when(studentDAO.findByUserEmail(existingEmail)).thenReturn(Optional.of(otherStudent));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> studentService.updateStudent(studentId, updateDTO)
            );
            assertEquals("A student with this email already exists", exception.getMessage());
            verify(studentDAO).findById(studentId);
            verify(studentDAO).findByUserEmail(existingEmail);
            verify(studentDAO, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when student not found")
        void shouldThrowUserNotFoundExceptionWhenStudentNotFound() {
            Long studentId = 999L;
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(1L, "updated@okcps.org", "Updated", "Student", "STUDENT"),
                    150,
                    "token",
                    new TeacherDTO(1L, null, "FIRST")
            );
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(
                    UserNotFoundException.class,
                    () -> studentService.updateStudent(studentId, updateDTO)
            );
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when new teacher not found")
        void shouldThrowUserNotFoundExceptionWhenNewTeacherNotFound() {
            Long studentId = 1L;
            Student existingStudent = createStudent(studentId, 100);
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(1L, existingStudent.getUser().getEmail(), "Updated", "Student", "STUDENT"),
                    150,
                    existingStudent.getToken(),
                    new TeacherDTO(999L, null, "FIRST")
            );
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(existingStudent));
            when(teacherDAO.findById(999L)).thenReturn(Optional.empty());
            assertThrows(
                    UserNotFoundException.class,
                    () -> studentService.updateStudent(studentId, updateDTO)
            );
        }

        @Test
        @DisplayName("Should allow update when email exists but is same student")
        void shouldAllowUpdateWhenEmailExistsButIsSameStudent() {
            Long studentId = 1L;
            Student existingStudent = createStudent(studentId, 100);
            String newEmail = "newemail@okcps.org";
            StudentDTO updateDTO = new StudentDTO(
                    studentId,
                    new UserDTO(existingStudent.getUser().getId(), newEmail, "Updated", "Student", "STUDENT"),
                    150,
                    existingStudent.getToken(),
                    new TeacherDTO(1L, null, "FIRST")
            );
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(existingStudent));
            when(studentDAO.findByUserEmail(newEmail)).thenReturn(Optional.of(existingStudent));
            when(studentDAO.save(any(Student.class))).thenReturn(existingStudent);
            StudentDTO result = studentService.updateStudent(studentId, updateDTO);
            assertNotNull(result);
            assertEquals(newEmail, result.getUser().getEmail());
            verify(studentDAO).findById(studentId);
            verify(studentDAO).findByUserEmail(newEmail);
            verify(studentDAO).save(existingStudent);
        }
    }

    @Nested
    @DisplayName("When deleting student")
    class WhenDeletingStudent {
        @Test
        @DisplayName("Should delete student successfully")
        void shouldDeleteStudentSuccessfully() {
            Long studentId = 1L;
            Student student = createStudent(studentId, 150);
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            studentService.deleteStudent(studentId);
            verify(studentDAO).findById(studentId);
            verify(studentDAO).delete(student);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when student not found")
        void shouldThrowUserNotFoundExceptionWhenStudentNotFound() {
            Long studentId = 999L;
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(
                    UserNotFoundException.class,
                    () -> studentService.deleteStudent(studentId)
            );
        }
    }

    // Helper methods
    private Student createStudent(Long id, Integer points) {
        Student student = new Student();
        student.setId(id);
        student.setPoints(points);
        student.generateToken();
        User user = createUser(id,
                "student" + id + "@okcps.org",
                "John", "Doe", Role.STUDENT);
        student.setUser(user);
        Teacher teacher = createTeacher(1L, GradeLevel.FIRST);
        student.setTeacher(teacher);
        return student;
    }

    private Teacher createTeacher(Long id, GradeLevel grade) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        User user = createUser(id + 100L,
                "teacher" + id + "@okcps.org",
                "Jane", "Smith", Role.TEACHER);
        teacher.setUser(user);
        teacher.setGrade(grade);
        return teacher;
    }

    private User createUser(Long id, String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }
}
