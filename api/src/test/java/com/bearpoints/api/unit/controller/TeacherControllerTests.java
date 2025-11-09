package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.TeacherController;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.exception.UserNotFoundException;
import com.bearpoints.api.service.TeacherService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
 * @version 1.0
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
        UserDTO userDTO = new UserDTO(id, email, firstName, lastName, "TEACHER");
        return new TeacherDTO(id, userDTO, grade.name());
    }

    @Nested
    @DisplayName("When retrieving all teachers")
    class WhenRetrievingAllTeachers {
        @Test
        @DisplayName("Should return paginated teachers with default parameters")
        void shouldReturnPaginatedTeachersWithDefaultParameters() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST),
                    createTeacherDTO(2L, "teacher2@okcps.org", "Jane", "Smith", GradeLevel.SECOND)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "lastName,asc");
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
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(1, 10), 15L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(1, 10, "email,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with DESC in uppercase")
        void shouldHandleSortParameterWithDescInUppercase() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "firstName,DESC");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with mixed case direction")
        void shouldHandleSortParameterWithMixedCaseDirection() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "firstName,DeSc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with single field (no direction)")
        void shouldHandleSortParameterWithSingleFieldNoDirection() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "email");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with invalid direction")
        void shouldHandleSortParameterWithInvalidDirection() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "lastName,invalid");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching teachers by email")
    class WhenSearchingTeachersByEmail {
        @Test
        @DisplayName("Should search teachers by email")
        void shouldSearchTeachersByEmail() {
            String email = "teacher";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, email + "1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByEmail(eq(email), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByEmail(email, 0, 20, "email");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByEmail(eq(email), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by email with ASC sort")
        void shouldSearchTeachersByEmailWithAscSort() {
            String email = "teacher";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, email + "1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByEmail(eq(email), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByEmail(email, 0, 20, "email,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByEmail(eq(email), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by email with DESC sort")
        void shouldSearchTeachersByEmailWithDescSort() {
            String email = "teacher";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, email + "1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByEmail(eq(email), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByEmail(email, 0, 20, "email,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByEmail(eq(email), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching teachers by first name")
    class WhenSearchingTeachersByFirstName {
        @Test
        @DisplayName("Should search teachers by first name")
        void shouldSearchTeachersByFirstName() {
            String firstName = "John";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", firstName, "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByFirstName(eq(firstName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByFirstName(firstName, 0, 20, "firstName");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByFirstName(eq(firstName), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by first name with ASC sort")
        void shouldSearchTeachersByFirstNameWithAscSort() {
            String firstName = "John";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", firstName, "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByFirstName(eq(firstName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByFirstName(firstName, 0, 20, "firstName,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByFirstName(eq(firstName), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by first name with DESC sort")
        void shouldSearchTeachersByFirstNameWithDescSort() {
            String firstName = "John";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", firstName, "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByFirstName(eq(firstName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByFirstName(firstName, 0, 20, "firstName,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByFirstName(eq(firstName), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching teachers by last name")
    class WhenSearchingTeachersByLastName {
        @Test
        @DisplayName("Should search teachers by last name")
        void shouldSearchTeachersByLastName() {
            String lastName = "Doe";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", lastName, GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByLastName(eq(lastName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByLastName(lastName, 0, 20, "lastName");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByLastName(eq(lastName), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by last name with ASC sort")
        void shouldSearchTeachersByLastNameWithAscSort() {
            String lastName = "Doe";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", lastName, GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByLastName(eq(lastName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByLastName(lastName, 0, 20, "lastName,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByLastName(eq(lastName), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by last name with DESC sort")
        void shouldSearchTeachersByLastNameWithDescSort() {
            String lastName = "Doe";
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", lastName, GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByLastName(eq(lastName), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByLastName(lastName, 0, 20, "lastName,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(teacherService).searchTeachersByLastName(eq(lastName), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching teachers by grade level")
    class WhenSearchingTeachersByGrade {
        @Test
        @DisplayName("Should search teachers by grade level")
        void shouldSearchTeachersByGradeLevel() {
            GradeLevel grade = GradeLevel.FIRST;
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", grade)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByGrade(eq(grade), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByGrade(grade, 0, 20, "lastName");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(grade, response.getBody().getContent().getFirst().getGrade());
            verify(teacherService).searchTeachersByGrade(eq(grade), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by grade level with ASC sort")
        void shouldSearchTeachersByGradeLevelWithAscSort() {
            GradeLevel grade = GradeLevel.SECOND;
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", grade)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByGrade(eq(grade), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByGrade(grade, 0, 20, "firstName,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(grade, response.getBody().getContent().getFirst().getGrade());
            verify(teacherService).searchTeachersByGrade(eq(grade), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search teachers by grade level with DESC sort")
        void shouldSearchTeachersByGradeLevelWithDescSort() {
            GradeLevel grade = GradeLevel.THIRD;
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", grade)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.searchTeachersByGrade(eq(grade), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.searchTeachersByGrade(grade, 0, 20, "lastName,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(grade, response.getBody().getContent().getFirst().getGrade());
            verify(teacherService).searchTeachersByGrade(eq(grade), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving teacher by ID")
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
                    .thenThrow(new UserNotFoundException("Teacher not found with id: " + teacherId));
            assertThrows(UserNotFoundException.class, () -> teacherController.getTeacherById(teacherId));
            verify(teacherService).getTeacherById(teacherId);
        }
    }

    @Nested
    @DisplayName("When creating teacher")
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
    @DisplayName("When updating teacher")
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
                    .thenThrow(new UserNotFoundException("Teacher not found with id: " + teacherId));
            assertThrows(
                    UserNotFoundException.class,
                    () -> teacherController.updateTeacher(teacherId, teacherDTO)
            );
            verify(teacherService).updateTeacher(teacherId, teacherDTO);
        }
    }

    @Nested
    @DisplayName("When deleting teacher")
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
            doThrow(new UserNotFoundException("Teacher not found with id: " + teacherId))
                    .when(teacherService).deleteTeacher(teacherId);
            assertThrows(
                    UserNotFoundException.class,
                    () -> teacherController.deleteTeacher(teacherId)
            );
            verify(teacherService).deleteTeacher(teacherId);
        }
    }

    @Nested
    @DisplayName("When testing sort parameter splitting")
    class WhenTestingSortParameterSplitting {
        @Test
        @DisplayName("Should handle sort parameter with multiple commas")
        void shouldHandleSortParameterWithMultipleCommas() {
            List<TeacherDTO> teachers = List.of(
                    createTeacherDTO(1L, "teacher1@okcps.org", "John", "Doe", GradeLevel.FIRST)
            );
            Page<TeacherDTO> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<TeacherDTO> expectedResponse = PagedResponseDTO.of(teacherPage);
            when(teacherService.getAllTeachers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<TeacherDTO>> response = teacherController.getAllTeachers(0, 20, "field1,field2,field3");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(teacherService).getAllTeachers(any(Pageable.class));
        }
    }
}
