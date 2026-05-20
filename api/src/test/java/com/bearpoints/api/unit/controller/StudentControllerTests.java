package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.StudentController;
import com.bearpoints.api.criteria.StudentSearchCriteria;
import com.bearpoints.api.dto.*;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentController}.
 * <p>Verifies functionality of student management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 *     <li>Classroom leaderboard functionality</li>
 *     <li>Token-based student retrieval</li>
 * </ul>
 *
 * @see StudentController
 * @version 1.2
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentController Unit Tests")
public class StudentControllerTests {
    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private StudentDTO createStudentDTO(Long id, String email, String firstName, String lastName, Integer points, String token, Long teacherId) {
        UserDTO userDTO = new UserDTO(id, email, firstName, lastName, Role.STUDENT.name(), null, id);
        TeacherDTO teacherDTO = teacherId != null
                ? new TeacherDTO(teacherId,
                new UserDTO(teacherId, "teacher@okcps.org", "Teacher", "User", Role.TEACHER.name(), teacherId, null),
                "FIRST") : null;
        return new StudentDTO(id, userDTO, points, token, teacherDTO);
    }

    @Nested
    @DisplayName("GET /api/students - When retrieving all students")
    class WhenRetrievingAllStudents {
        @Test
        @DisplayName("Should return paginated students with default parameters")
        void shouldReturnPaginatedStudentsWithDefaultParameters() {
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", 1L),
                    createStudentDTO(2L, "student2@okcps.org", "Jane", "Smith", 150, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    2L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.getAllStudents(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController
                    .getAllStudents(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(studentService).getAllStudents(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "user.email")),
                    15L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.getAllStudents(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController
                    .getAllStudents(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "user.email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).getAllStudents(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", 1L)
            );
            Sort multiSort = Sort.by(
                    Sort.Order.desc("firstName"),
                    Sort.Order.asc("lastName"),
                    Sort.Order.desc("email"),
                    Sort.Order.asc("teacher.id"),
                    Sort.Order.desc("points"),
                    Sort.Order.asc("token")
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.getAllStudents(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController
                    .getAllStudents(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(studentService).getAllStudents(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/students/search - When searching students")
    class WhenSearchingStudents {
        @Test
        @DisplayName("Should search students with email criteria")
        void shouldSearchStudentsWithEmailCriteria() {
            String email = "student";
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, email + "1@okcps.org", "John", "Doe", 100, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(email,
                    null, null, null, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search students with first name criteria")
        void shouldSearchStudentsWithFirstNameCriteria() {
            String firstName = "John";
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", firstName, "Doe", 100, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(null,
                    firstName, null, null, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search students with last name criteria")
        void shouldSearchStudentsWithLastNameCriteria() {
            String lastName = "John";
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", lastName, 100, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(null,
                    null, lastName, null, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search students with teacher ID criteria")
        void shouldSearchStudentsWithTeacherIdCriteria() {
            Long teacherId = 1L;
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", teacherId)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "teacher.id")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(null,
                    null, null, teacherId, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search students with points range criteria")
        void shouldSearchStudentsWithPointsRangeCriteria() {
            int points = 100;
            Integer minPoints = points - 50;
            Integer maxPoints = points + 50;
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", points, "example-token", 1L)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "points")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(null,
                    null, null, null, minPoints, maxPoints,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "points")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search students with combined criteria")
        void shouldSearchStudentsWithCombinedCriteria() {
            String email = "student";
            String firstName = "John";
            Long teacherId = 1L;
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, email + "1@okcps.org", firstName, "Doe", 100, "example-token", teacherId)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.searchStudents(any(StudentSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController.searchStudents(email,
                    firstName, null, teacherId, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentService).searchStudents(any(StudentSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/students/leaderboard/{id} - When retrieving classroom leaderboard")
    class WhenRetrievingClassroomLeaderboard {
        @Test
        @DisplayName("Should return classroom leaderboard with default parameters")
        void shouldReturnClassroomLeaderboardWithDefaultParameters() {
            Long teacherId = 1L;
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", teacherId),
                    createStudentDTO(2L, "student2@okcps.org", "Jane", "Smith", 150, "example-token", teacherId)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "points")),
                    1L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.getClassRoomLeaderboard(eq(teacherId), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController
                    .getClassRoomLeaderboard(teacherId, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "points")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(studentService).getClassRoomLeaderboard(eq(teacherId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination for leaderboard")
        void shouldHandleCustomPaginationForLeaderboard() {
            Long teacherId = 1L;
            List<StudentDTO> students = List.of(
                    createStudentDTO(1L, "student1@okcps.org", "John", "Doe", 100, "example-token", teacherId),
                    createStudentDTO(2L, "student2@okcps.org", "Jane", "Smith", 150, "example-token", teacherId)
            );
            Page<StudentDTO> studentPage = new PageImpl<>(students,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "points")),
                    15L);
            PagedResponseDTO<StudentDTO> expectedResponse = PagedResponseDTO.of(studentPage);
            when(studentService.getClassRoomLeaderboard(eq(teacherId), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentDTO>> response = studentController
                    .getClassRoomLeaderboard(teacherId, PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "points")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(studentService).getClassRoomLeaderboard(eq(teacherId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/students/{id} - When retrieving student by ID")
    class WhenRetrievingStudentById {
        @Test
        @DisplayName("Should return student when found")
        void shouldReturnStudentWhenFound() {
            Long studentId = 1L;
            StudentDTO student = createStudentDTO(studentId, "student1@okcps.org", "John",
                    "Doe", 100, "example-token", 1L);
            when(studentService.getStudentById(studentId)).thenReturn(student);
            ResponseEntity<StudentDTO> response = studentController.getStudentById(studentId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentId, response.getBody().getId());
            verify(studentService).getStudentById(studentId);
        }

        @Test
        @DisplayName("Should return 404 when student not found")
        void shouldReturn404WhenStudentNotFound() {
            Long studentId = 999L;
            when(studentService.getStudentById(studentId))
                    .thenThrow(new ResourceNotFoundException("Student not found with ID: " + studentId));
            assertThrows(ResourceNotFoundException.class, () -> studentController.getStudentById(studentId));
            verify(studentService).getStudentById(studentId);
        }
    }

    @Nested
    @DisplayName("GET /api/students/token/{token} - When retrieving student by token")
    class WhenRetrievingStudentByToken {
        @Test
        @DisplayName("Should return student when found by token")
        void shouldReturnStudentWhenFoundByToken() {
            String token = "example-token";
            StudentDTO student = createStudentDTO(1L, "student1@okcps.org", "John",
                    "Doe", 100, token, 1L);
            when(studentService.getStudentByToken(token)).thenReturn(student);
            ResponseEntity<StudentDTO> response = studentController.getStudentByToken(token);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(token, response.getBody().getToken());
            verify(studentService).getStudentByToken(token);
        }

        @Test
        @DisplayName("Should return 404 when student not found by token")
        void shouldReturn404WhenStudentNotFoundByToken() {
            String token = "invalid-token";
            when(studentService.getStudentByToken(token))
                    .thenThrow(new ResourceNotFoundException("Student not found with token: " + token));
            assertThrows(ResourceNotFoundException.class, () -> studentController.getStudentByToken(token));
            verify(studentService).getStudentByToken(token);
        }
    }

    @Nested
    @DisplayName("POST /api/students - When creating student")
    class WhenCreatingStudent {
        @Test
        @DisplayName("Should create new student and return 201 status")
        void shouldCreateNewStudentAndReturn201Status() {
            Long studentId = 1L;
            String email = "new.student@okcps.org";
            String firstName = "New";
            String lastName = "Student";
            Integer points = 0;
            Long teacherId = 1L;
            StudentDTO studentDTO = createStudentDTO(null, email, firstName, lastName, points, null, teacherId);
            StudentDTO createdStudent = createStudentDTO(studentId, email, firstName, lastName, points, "token", teacherId);
            when(studentService.createStudent(studentDTO)).thenReturn(createdStudent);
            ResponseEntity<StudentDTO> response = studentController.createStudent(studentDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentId, response.getBody().getId());
            assertEquals(email, response.getBody().getUser().getEmail());
            assertEquals(firstName, response.getBody().getUser().getFirstName());
            assertEquals(lastName, response.getBody().getUser().getLastName());
            assertEquals(Role.STUDENT, response.getBody().getUser().getRole());
            assertEquals(points, response.getBody().getPoints());
            verify(studentService).createStudent(studentDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id} - When updating student")
    class WhenUpdatingStudent {
        @Test
        @DisplayName("Should update existing student and return 200 status")
        void shouldUpdateExistingStudentAndReturn200Status() {
            Long studentId = 1L;
            String email = "updated.student@okcps.org";
            String firstName = "Updated";
            String lastName = "User";
            Integer points = 150;
            Long teacherId = 2L;
            String token = "token";
            StudentDTO studentDTO = createStudentDTO(studentId, email, firstName, lastName, points, token, teacherId);
            StudentDTO updatedStudent = createStudentDTO(studentId, email, firstName, lastName, points, token, teacherId);
            when(studentService.updateStudent(studentId, studentDTO)).thenReturn(updatedStudent);
            ResponseEntity<StudentDTO> response = studentController.updateStudent(studentId, studentDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentId, response.getBody().getId());
            assertEquals(email, response.getBody().getUser().getEmail());
            assertEquals(firstName, response.getBody().getUser().getFirstName());
            assertEquals(lastName, response.getBody().getUser().getLastName());
            assertEquals(Role.STUDENT, response.getBody().getUser().getRole());
            assertEquals(points, response.getBody().getPoints());
            verify(studentService).updateStudent(studentId, studentDTO);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent student")
        void shouldReturn404WhenUpdatingNonExistentStudent() {
            Long studentId = 999L;
            StudentDTO studentDTO = createStudentDTO(studentId, "nonexistent@okcps.org",
                    "Nonexistent", "Student", 0, "token", 1L);
            when(studentService.updateStudent(studentId, studentDTO))
                    .thenThrow(new ResourceNotFoundException("Student not found with ID: " + studentId));
            assertThrows(ResourceNotFoundException.class, () -> studentController.updateStudent(studentId, studentDTO));
            verify(studentService).updateStudent(studentId, studentDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id} - When deleting student")
    class WhenDeletingStudent {
        @Test
        @DisplayName("Should delete student and return 204 status")
        void shouldDeleteStudentAndReturn204Status() {
            Long studentId = 1L;
            ResponseEntity<Void> response = studentController.deleteStudent(studentId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(studentService).deleteStudent(studentId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent student")
        void shouldReturn404WhenDeletingNonExistentStudent() {
            Long studentId = 999L;
            doThrow(new ResourceNotFoundException("Student not found with ID: " + studentId))
                    .when(studentService).deleteStudent(studentId);
            assertThrows(ResourceNotFoundException.class, () -> studentController.deleteStudent(studentId));
            verify(studentService).deleteStudent(studentId);
        }
    }
}
