package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.TeacherController;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.TeacherService;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherController}.
 * <p>Verifies functionality of teacher management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 *     <li>Grade level search with enum conversion</li>
 * </ul>
 *
 * @see TeacherController
 * @version 1.2
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherController Unit Tests")
public class TeacherControllerTests {
    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private TeacherController teacherController;

    private TeacherDTO createTeacherDTO(Long id, String email, String firstName, String lastName, GradeLevel grade) {
        UserDTO userDTO = new UserDTO(id, email, firstName, lastName, "TEACHER", id, null);
        return new TeacherDTO(id, userDTO, grade.name());
    }

    @Nested
    @DisplayName("GET /api/teachers - When retrieving all teachers")
    class WhenRetrievingAllTeachers {
        @Test
        @DisplayName("Should return paginated teachers with default parameters")
        void shouldReturnPaginatedTeachersWithDefaultParameters() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST),
                    createTeacherDTO(2L, "teacher2@okcps.org", "Jane", "Smith", GradeLevel.SECOND)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    2L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController
                    .getAllTeachers(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "user.email")),
                    15L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController
                    .getAllTeachers(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "user.email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Sort multiSort = Sort.by(
                    Sort.Order.asc("user.email"),
                    Sort.Order.desc("user.lastName"),
                    Sort.Order.asc("user.firstName"),
                    Sort.Order.desc("grade")
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController
                    .getAllTeachers(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/teachers/search - When searching teachers")
    class WhenSearchingTeachers {
        @Test
        @DisplayName("Should search teachers by email")
        void shouldSearchTeachersByEmail() {
            String email = "teacher";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, email + "1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.email")),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachers(email,
                    null, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by first name")
        void shouldSearchTeachersByFirstName() {
            String firstName = "John";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", firstName, "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.firstName")),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachers(null,
                    firstName, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.firstName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by last name")
        void shouldSearchTeachersByLastName() {
            String lastName = "Doe";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", lastName, GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachers(null,
                    null, lastName, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by grade level")
        void shouldSearchTeachersByGradeLevel() {
            GradeLevel grade = GradeLevel.FIRST;
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", grade)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "grade")),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachers(null,
                    null, null, grade,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "grade")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(grade, response.getBody().getContent().getFirst().getGrade());
            verify(teacherService).searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers with combined criteria")
        void shouldSearchTeachersWithCombinedCriteria() {
            String email = "teacher";
            String firstName = "John";
            String lastName = "Doe";
            GradeLevel grade = GradeLevel.FIRST;
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, email + "1@okcps.org", firstName, lastName, grade)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")),
                    1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachers(email,
                    firstName, lastName, grade,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "user.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(grade, response.getBody().getContent().getFirst().getGrade());
            verify(teacherService).searchTeachers(any(TeacherSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/teachers/{id} - When retrieving teacher by ID")
    class WhenRetrievingTeacherById {
        @Test
        @DisplayName("Should return teacher when found")
        void shouldReturnTeacherWhenFound() {
            Long teacherId = 1L;
            TeacherDTO teacher = createTeacherDTO(teacherId, "teacher@okcps.org", "John", "Doe", GradeLevel.FIRST);
            when(teacherService.getTeacherById(teacherId)).thenReturn(teacher);
            ResponseEntity<TeacherDTO> response = teacherController.getTeacherById(teacherId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(teacherId, response.getBody().getId());
            assertEquals(teacher.getUser().getEmail(), response.getBody().getUser().getEmail());
            verify(teacherService).getTeacherById(teacherId);
        }

        @Test
        @DisplayName("Should return 404 when teacher not found")
        void shouldReturn404WhenTeacherNotFound() {
            Long teacherId = 999L;
            when(teacherService.getTeacherById(teacherId))
                    .thenThrow(new ResourceNotFoundException("Teacher not found with id: " + teacherId));
            assertThrows(ResourceNotFoundException.class, () -> teacherController.getTeacherById(teacherId));
            verify(teacherService).getTeacherById(teacherId);
        }
    }

    @Nested
    @DisplayName("POST /api/teachers - When creating teacher")
    class WhenCreatingTeacher {
        @Test
        @DisplayName("Should create new teacher and return 201 status")
        void shouldCreateNewTeacherAndReturn201Status() {
            Long teacherId = 1L;
            String email = "new.teacher@okcps.org";
            String firstName = "New";
            String lastName = "Teacher";
            GradeLevel grade = GradeLevel.FIRST;
            TeacherDTO teacherDTO = createTeacherDTO(null, email, firstName, lastName, grade);
            TeacherDTO createdTeacher = createTeacherDTO(teacherId, email, firstName, lastName, grade);
            when(teacherService.createTeacher(teacherDTO)).thenReturn(createdTeacher);
            ResponseEntity<TeacherDTO> response = teacherController.createTeacher(teacherDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(teacherId, response.getBody().getId());
            assertEquals(email, response.getBody().getUser().getEmail());
            assertEquals(firstName, response.getBody().getUser().getFirstName());
            assertEquals(lastName, response.getBody().getUser().getLastName());
            assertEquals(Role.TEACHER, response.getBody().getUser().getRole());
            assertEquals(grade, response.getBody().getGrade());
            verify(teacherService).createTeacher(teacherDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/teachers/{id} - When updating teacher")
    class WhenUpdatingTeacher {
        @Test
        @DisplayName("Should update existing teacher and return 200 status")
        void shouldUpdateExistingTeacherAndReturn200Status() {
            Long teacherId = 1L;
            String email = "updated.teacher@okcps.org";
            String firstName = "Updated";
            String lastName = "User";
            GradeLevel grade = GradeLevel.SECOND;
            TeacherDTO teacherDTO = createTeacherDTO(teacherId, email, firstName, lastName, grade);
            TeacherDTO updatedTeacher = createTeacherDTO(teacherId, email, firstName, lastName, grade);
            when(teacherService.updateTeacher(teacherId, teacherDTO)).thenReturn(updatedTeacher);
            ResponseEntity<TeacherDTO> response = teacherController.updateTeacher(teacherId, teacherDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(teacherId, response.getBody().getId());
            assertEquals(email, response.getBody().getUser().getEmail());
            assertEquals(firstName, response.getBody().getUser().getFirstName());
            assertEquals(lastName, response.getBody().getUser().getLastName());
            assertEquals(Role.TEACHER, response.getBody().getUser().getRole());
            assertEquals(grade, response.getBody().getGrade());
            verify(teacherService).updateTeacher(teacherId, teacherDTO);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent teacher")
        void shouldReturn404WhenUpdatingNonExistentTeacher() {
            Long teacherId = 999L;
            TeacherDTO teacherDTO = createTeacherDTO(teacherId, "nonexistent@okcps.org", "Nonexistent", "Teacher", GradeLevel.FIRST);
            when(teacherService.updateTeacher(teacherId, teacherDTO))
                    .thenThrow(new ResourceNotFoundException("Teacher not found with id: " + teacherId));
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherController.updateTeacher(teacherId, teacherDTO)
            );
            verify(teacherService).updateTeacher(teacherId, teacherDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/teachers/{id} - When deleting teacher")
    class WhenDeletingTeacher {
        @Test
        @DisplayName("Should delete teacher and return 204 status")
        void shouldDeleteTeacherAndReturn204Status() {
            Long teacherId = 1L;
            ResponseEntity<TeacherDTO> response = teacherController.deleteTeacher(teacherId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(teacherService).deleteTeacher(teacherId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent teacher")
        void shouldReturn404WhenDeletingNonExistentTeacher() {
            Long teacherId = 999L;
            doThrow(new ResourceNotFoundException("Teacher not found with id: " + teacherId))
                    .when(teacherService).deleteTeacher(teacherId);
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teacherController.deleteTeacher(teacherId)
            );
            verify(teacherService).deleteTeacher(teacherId);
        }
    }
}
