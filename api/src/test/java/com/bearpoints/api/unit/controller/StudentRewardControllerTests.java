package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.StudentRewardController;
import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentRewardDTO;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.StudentRewardService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentRewardController}.
 * <p>Verifies functionality of student reward management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity and entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see StudentRewardController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentRewardController Unit Tests")
public class StudentRewardControllerTests {
    @Mock
    private StudentRewardService studentRewardService;

    @InjectMocks
    private StudentRewardController studentRewardController;

    private StudentRewardDTO createStudentRewardDTO(Long id, Long studentId, Long itemId) {
        return new StudentRewardDTO(id, studentId, itemId, null, null, null, null);
    }

    @Nested
    @DisplayName("GET /api/rewards - When retrieving all student rewards")
    class WhenRetrievingAllStudentRewards {
        @Test
        @DisplayName("Should return paginated student rewards with default parameters")
        void shouldReturnPaginatedStudentRewardsWithDefaultParameters() {
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L),
                    createStudentRewardDTO(2L, 2L, 1L),
                    createStudentRewardDTO(3L, 1L, 2L),
                    createStudentRewardDTO(4L, 2L, 2L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    4L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.getAllStudentRewards(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .getAllStudentRewards(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(4, response.getBody().getContent().size());
            verify(studentRewardService).getAllStudentRewards(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "student.lastName")),
                    15L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.getAllStudentRewards(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .getAllStudentRewards(PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "student.lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).getAllStudentRewards(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Sort multiSort = Sort.by(
                    Sort.Order.desc("redeemedAt"),
                    Sort.Order.asc("student.lastName"),
                    Sort.Order.asc("student.teacher.grade")
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.getAllStudentRewards(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .getAllStudentRewards(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(studentRewardService).getAllStudentRewards(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/rewards/search - When searching student rewards")
    class WhenSearchingStudentRewards {
        @Test
        @DisplayName("Should search student rewards with student name criteria")
        void shouldSearchStudentRewardsWithStudentNameCriteria() {
            String studentName = "Test";
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(studentName, null, null, null, null,
                            null, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with student id criteria")
        void shouldSearchStudentRewardsWithStudentIdCriteria() {
            Long studentId = 1L;
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(null, studentId, null, null, null,
                            null, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with item name criteria")
        void shouldSearchStudentRewardsWithItemNameCriteria() {
            String itemName = "Test";
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(null, null, itemName, null, null,
                            null, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with item id criteria")
        void shouldSearchStudentRewardsWithItemIdCriteria() {
            Long itemId = 1L;
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(null, null, null, itemId, null,
                            null, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with points used range criteria")
        void shouldSearchStudentRewardsWithPointsUsedRangeCriteria() {
            Integer minPointsUsed = 5;
            Integer maxPointsUsed = 15;
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(null, null, null, null, minPointsUsed,
                            maxPointsUsed, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with redeemed date range criteria")
        void shouldSearchStudentRewardsWithRedeemedDateRangeCriteria() {
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endDate = LocalDateTime.now();
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(null, null, null, null, null,
                            null, startDate, endDate,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search student rewards with combined criteria")
        void shouldSearchStudentRewardsWithCombinedCriteria() {
            String studentName = "Test";
            Long studentId = 1L;
            String itemName = "Test";
            Long itemId = 1L;
            Integer minPointsUsed = 5;
            Integer maxPointsUsed = 15;
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endDate = LocalDateTime.now();
            List<StudentRewardDTO> studentRewards = List.of(
                    createStudentRewardDTO(1L, 1L, 1L)
            );
            Page<StudentRewardDTO> studentRewardPage = new PageImpl<>(studentRewards,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")),
                    1L);
            PagedResponseDTO<StudentRewardDTO> expectedResponse = PagedResponseDTO.of(studentRewardPage);
            when(studentRewardService.searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<StudentRewardDTO>> response = studentRewardController
                    .searchStudentRewards(studentName, studentId, itemName, itemId, minPointsUsed, maxPointsUsed,
                            startDate, endDate,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "redeemedAt")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(studentRewardService).searchStudentRewards(any(StudentRewardSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/rewards/{id} - When retrieving student rewards by ID")
    class WhenRetrievingStudentRewardById {
        @Test
        @DisplayName("Should return student reward when found")
        void shouldReturnStudentRewardByIdWhenFound() {
            Long studentRewardId = 1L;
            StudentRewardDTO studentReward = createStudentRewardDTO(studentRewardId, 1L, 1L);
            when(studentRewardService.getStudentRewardById(studentRewardId)).thenReturn(studentReward);
            ResponseEntity<StudentRewardDTO> response = studentRewardController.getStudentRewardById(studentRewardId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentRewardId, response.getBody().getId());
            verify(studentRewardService).getStudentRewardById(studentRewardId);
        }

        @Test
        @DisplayName("Should return 404 when student reward not found")
        void shouldReturn404WhenStudentRewardByIdNotFound() {
            Long studentRewardId = 9999L;
            when(studentRewardService.getStudentRewardById(studentRewardId))
                    .thenThrow(new ResourceNotFoundException("Student reward not found with ID: " + studentRewardId));
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardController.getStudentRewardById(studentRewardId));
            verify(studentRewardService).getStudentRewardById(studentRewardId);
        }
    }

    @Nested
    @DisplayName("POST /api/rewards - When creating student reward")
    class WhenCreatingStudentReward {
        @Test
        @DisplayName("Should create new student reward and return 201 status")
        void shouldCreateNewStudentRewardAndReturn201Status() {
            Long studentRewardId = 1L;
            Long studentId = 1L;
            Long itemId = 1L;
            StudentRewardDTO studentRewardDTO = createStudentRewardDTO(null, studentId, itemId);
            StudentRewardDTO createdStudentReward = createStudentRewardDTO(studentRewardId, studentId, itemId);
            when(studentRewardService.createStudentReward(studentRewardDTO)).thenReturn(createdStudentReward);
            ResponseEntity<StudentRewardDTO> response = studentRewardController.createStudentReward(studentRewardDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentRewardId, response.getBody().getId());
            assertEquals(studentId, response.getBody().getStudentId());
            assertEquals(itemId, response.getBody().getItemId());
            verify(studentRewardService).createStudentReward(studentRewardDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/rewards/{id} - When updating student reward")
    class WhenUpdatingStudentReward {
        @Test
        @DisplayName("Should update existing student reward and return 200 status")
        void shouldUpdateExistingStudentRewardAndReturn200Status() {
            Long studentRewardId = 1L;
            Long studentId = 2L;
            Long itemId = 2L;
            StudentRewardDTO studentRewardDTO = createStudentRewardDTO(null, studentId, itemId);
            StudentRewardDTO updatedStudentReward = createStudentRewardDTO(studentRewardId, studentId, itemId);
            when(studentRewardService.updateStudentReward(studentRewardId, studentRewardDTO)).thenReturn(updatedStudentReward);
            ResponseEntity<StudentRewardDTO> response = studentRewardController.updateStudentReward(studentRewardId, studentRewardDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(studentId, response.getBody().getStudentId());
            assertEquals(itemId, response.getBody().getItemId());
            verify(studentRewardService).updateStudentReward(studentRewardId, studentRewardDTO);

        }

        @Test
        @DisplayName("Should return 404 when updating non-existent student reward")
        void shouldReturn404WhenUpdatingNonExistentStudentReward() {
            Long studentRewardId = 9999L;
            StudentRewardDTO studentRewardDTO = createStudentRewardDTO(null, 1L, 1L);
            when(studentRewardService.updateStudentReward(studentRewardId, studentRewardDTO))
                    .thenThrow(new ResourceNotFoundException("Student reward not found with ID: " + studentRewardId));
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardController.updateStudentReward(studentRewardId, studentRewardDTO));
            verify(studentRewardService).updateStudentReward(studentRewardId, studentRewardDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/rewards/{id} - When deleting student rewards")
    class WhenDeletingStudentReward {
        @Test
        @DisplayName("Should delete student reward and return 204 status")
        void shouldDeleteStudentRewardAndReturn204Status() {
            Long studentRewardId = 1L;
            ResponseEntity<Void> response = studentRewardController.deleteStudentReward(studentRewardId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(studentRewardService).deleteStudentReward(studentRewardId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent student reward")
        void shouldReturn404WhenDeletingNonExistentStudentReward() {
            Long studentRewardId = 999L;
            doThrow(new ResourceNotFoundException("Student reward not found with ID: " + studentRewardId))
                    .when(studentRewardService).deleteStudentReward(studentRewardId);
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardController.deleteStudentReward(studentRewardId));
            verify(studentRewardService).deleteStudentReward(studentRewardId);
        }
    }
}
