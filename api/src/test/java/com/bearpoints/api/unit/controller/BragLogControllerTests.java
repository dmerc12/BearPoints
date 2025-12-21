package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.BragLogController;
import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.BragLogService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BragLogController}.
 * <p>Verifies functionality of public brag log management endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see BragLogController
 * @version 2.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BragLogController Unit Tests")
public class BragLogControllerTests {
    @Mock
    private BragLogService bragLogService;

    @InjectMocks
    private BragLogController bragLogController;

    // DEPRECATED
    private BragLogRequest bragLogRequest;
    private BragLog createdBragLog;

    // DEPRECATED
    @BeforeEach
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        bragLogRequest = new BragLogRequest(1L, 1L, Set.of(1L, 2L), "Test notes");
        createdBragLog = new BragLog();
        createdBragLog.setId(100L);
    }

    private BragLogDTO createBragLogDTO(Long id, Long teacherId, Long studentId, String studentName, String teacherName) {
        BehaviorTypeDTO behavior1 = new BehaviorTypeDTO(1L, "Participated", 2, true);
        BehaviorTypeDTO behavior2 = new BehaviorTypeDTO(2L, "Listened", 1, true);
        Integer pointsGenerated = behavior1.getPointValue() + behavior2.getPointValue();
        return new BragLogDTO(id, studentId, teacherId, Set.of(1L, 2L), "test notes" + id,
                studentName, teacherName, GradeLevel.SECOND,
                Set.of(behavior1, behavior2), pointsGenerated, LocalDateTime.now());
    }

    @Nested
    @DisplayName("When retrieving all brag logs")
    class RetrieveAllBragLogs {
        @Test
        @DisplayName("Should return paginated brag logs with default parameters")
        void shouldReturnPaginatedBragLogsWithDefaultParameters() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(0, 20, "timestamp,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(1, 10, "timestamp,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with DESC in uppercase")
        void shouldHandleSortingParameterWithDESCInUppercase() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(1, 10, "timestamp,DESC");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with mixed case direction")
        void shouldHandleSortParameterWithMixedCaseDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(1, 10, "timestamp,DeSc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with single field (no direction)")
        void shouldHandleSortParameterWithSingleFieldNoDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(1, 10, "timestamp");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with invalid direction")
        void shouldHandleSortParameterWithInvalidDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.getAllBragLogs(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .getAllBragLogs(1, 10, "timestamp,invalid");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(bragLogService).getAllBragLogs(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching brag logs")
    class WhenSearchingBragLogs {
        @Test
        @DisplayName("Should search brag logs with student name criteria")
        void shouldSearchBragLogsWithStudentNameCriteria() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .searchBragLogs("J", null, null, null, null,
                            null, null, null, null, null,
                            1, 10, "timestamp,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getTotalElements());
            verify(bragLogService).searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with DESC in uppercase")
        void shouldHandleSortingParameterWithDESCInUppercase() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .searchBragLogs("J", null, null, null, null,
                            null, null, null, null, null,
                            1, 10, "timestamp,DESC");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getTotalElements());
            verify(bragLogService).searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with mixed case direction")
        void shouldHandleSortParameterWithMixedCaseDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .searchBragLogs("J", null, null, null, null,
                            null, null, null, null, null,
                            1, 10, "timestamp,DeSc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getTotalElements());
            verify(bragLogService).searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with single field (no direction)")
        void shouldHandleSortParameterWithSingleFieldNoDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .searchBragLogs("J", null, null, null, null,
                            null, null, null, null, null,
                            1, 10, "timestamp");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getTotalElements());
            verify(bragLogService).searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with invalid direction")
        void shouldHandleSortParameterWithInvalidDirection() {
            List<BragLogDTO> bragLogs = List.of(
                    createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith"),
                    createBragLogDTO(2L, 2L, 2L, "Bill Johnson", "Alice Garcia")
            );
            Page<BragLogDTO> bragLogPage = new PageImpl<>(bragLogs, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BragLogDTO> expectedResponse = PagedResponseDTO.of(bragLogPage);
            when(bragLogService.searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BragLogDTO>> response = bragLogController
                    .searchBragLogs("J", null, null, null, null,
                            null, null, null, null, null,
                            1, 10, "timestamp,invalid");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getTotalElements());
            verify(bragLogService).searchBragLogs(any(BragLogSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving brag log by ID")
    class WhenRetrievingBragLogById {
        @Test
        @DisplayName("Should return brag log when found")
        void shouldReturnBragLogWhenFound() {
            Long bragLogId = 1L;
            BragLogDTO bragLogDTO = createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith");
            when(bragLogService.getBragLogById(bragLogId)).thenReturn(bragLogDTO);
            ResponseEntity<BragLogDTO> response = bragLogController.getBragLogById(bragLogId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(bragLogId, response.getBody().getId());
            verify(bragLogService).getBragLogById(bragLogId);
        }

        @Test
        @DisplayName("Should return 404 when brag log not found")
        void shouldReturn404WhenBragLogNotFound() {
            Long bragLogId = 9990L;
            when(bragLogService.getBragLogById(bragLogId))
                    .thenThrow(new ResourceNotFoundException("Brag log not found with ID: " + bragLogId));
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogController.getBragLogById(bragLogId));
            verify(bragLogService).getBragLogById(bragLogId);
        }
    }

    @Nested
    @DisplayName("When creating brag log")
    class WhenCreatingBragLog {
        @Test
        @DisplayName("Should create new brag log and return 201 status")
        void shouldCreateNewBragLogAndReturn201Status() {
            BragLogDTO createdBragLog = createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith");
            BragLogDTO bragLogDTO = new BragLogDTO(null, createdBragLog.getStudentId(), null,
                    createdBragLog.getBehaviorIds(), "test notes 1", null, null,
                    null, null, null, null);
            when(bragLogService.createBragLog(bragLogDTO)).thenReturn(createdBragLog);
            ResponseEntity<BragLogDTO> response = bragLogController.createBragLog(bragLogDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(createdBragLog.getId(), response.getBody().getId());
            verify(bragLogService).createBragLog(bragLogDTO);
        }
    }

    @Nested
    @DisplayName("When updating brag log")
    class WhenUpdatingBragLog {
        @Test
        @DisplayName("Should update existing brag log and return 200 status")
        void shouldCreateNewBragLogAndReturn201Status() {
            BragLogDTO existingBragLog = createBragLogDTO(1L, 1L, 1L, "John Doe", "Jane Smith");
            BragLogDTO updatedBragLog = createBragLogDTO(1L, 2L, 2L, "Bill Johnson", "Alice Garcia");
            BragLogDTO bragLogDTO = new BragLogDTO(null, 2L, null,
                    existingBragLog.getBehaviorIds(), updatedBragLog.getNotes(), null, null,
                    null, null, null, null);
            when(bragLogService.updateBragLog(existingBragLog.getId(), bragLogDTO)).thenReturn(updatedBragLog);
            ResponseEntity<BragLogDTO> response = bragLogController.updateBragLog(existingBragLog.getId(), bragLogDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(existingBragLog.getId(), response.getBody().getId());
            verify(bragLogService).updateBragLog(existingBragLog.getId(), bragLogDTO);
        }

        @Test
        @DisplayName("Should return 404 when brag log not found")
        void shouldReturn404WhenBragLogNotFound() {
            Long bragLogId = 9990L;
            BragLogDTO bragLogDTO = new BragLogDTO(null, 2L, null,
                    Set.of(1L, 2L), "notes", null, null,
                    null, null, null, null);
            when(bragLogService.updateBragLog(bragLogId, bragLogDTO))
                    .thenThrow(new ResourceNotFoundException("Brag log not found with ID: " + bragLogId));
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogController.updateBragLog(bragLogId, bragLogDTO));
            verify(bragLogService).updateBragLog(bragLogId, bragLogDTO);
        }
    }

    @Nested
    @DisplayName("When deleting brag log")
    class WhenDeletingBragLog {
        @Test
        @DisplayName("Should delete brag log and return 204 status")
        void shouldDeleteBragLogAndReturn204Status() {
            Long bragLogId = 1L;
            ResponseEntity<Void> response = bragLogController.deleteBragLog(bragLogId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(bragLogService).deleteBragLog(bragLogId);
        }

        @Test
        @DisplayName("Should return 404 when brag log not found")
        void shouldReturn404WhenBragLogNotFound() {
            Long bragLogId = 9990L;
            doThrow(new ResourceNotFoundException("Brag log not found with ID: " + bragLogId))
                    .when(bragLogService).deleteBragLog(bragLogId);
            assertThrows(ResourceNotFoundException.class,
                    () -> bragLogController.deleteBragLog(bragLogId));
            verify(bragLogService).deleteBragLog(bragLogId);
        }
    }

    @Nested
    @DisplayName("DEPRECATED tests")
    class DeprecatedTests {
        /**
         * Tests successful brag log submission scenarios.
         */
        @Nested
        @DisplayName("Successful submission scenarios")
        class SuccessfulScenarios {
            /**
             * Tests that valid requests are properly processed.
             * <p>Verifies:
             * <ul>
             *     <li>HTTP 201 Created status</li>
             *     <li>Location header contains correct resource path</li>
             *     <li>Response body is empty</li>
             * </ul>
             */
            @Test
            @DisplayName("POST /brag-logs returns 201 with location header")
            void submitBragLog_ValidRequest_ReturnsCreated() {
                when(bragLogService.submitBragLog(any(BragLogRequest.class))).thenReturn(createdBragLog);
                ResponseEntity<Void> response = bragLogController.submitBragLog(bragLogRequest);
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                URI location = response.getHeaders().getLocation();
                assertNotNull(location);
                assertTrue(location.toString().endsWith("api/brag-logs/100"));
                assertNull(response.getBody());
            }
        }

        /**
         * Tests error scenarios and exception handling.
         */
        @Nested
        @DisplayName("Error scenarios")
        class ErrorScenarios {
            /**
             * Tests handling of invalid student ID.
             * <p>Verifies:
             * <ul>
             *     <li>Service exception is propagated</li>
             *     <li>HTTP 400 Bad Request status</li>
             * </ul>
             */
            @Test
            @DisplayName("Invalid student ID returns 400")
            void submitBragLog_InvalidStudent_ReturnsBadRequest() {
                when(bragLogService.submitBragLog(bragLogRequest))
                        .thenThrow(new IllegalArgumentException("Invalid student ID"));
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> bragLogController.submitBragLog(bragLogRequest));
                assertEquals("Invalid student ID", exception.getMessage());
            }

            /**
             * Tests handling of invalid teacher ID.
             * <p>Verifies:
             * <ul>
             *     <li>Service exception is propagated</li>
             *     <li>HTTP 400 Bad Request status</li>
             * </ul>
             */
            @Test
            @DisplayName("Invalid teacher ID returns 400")
            void submitBragLog_InvalidTeacher_ReturnsBadRequest() {
                when(bragLogService.submitBragLog(bragLogRequest))
                        .thenThrow(new IllegalArgumentException("Invalid teacher ID"));
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> bragLogController.submitBragLog(bragLogRequest));
                assertEquals("Invalid teacher ID", exception.getMessage());
            }

            /**
             * Tests handling of mismatched student-teacher relationship.
             * <p>Verifies:
             * <ul>
             *     <li>Service exception is propagated</li>
             *     <li>HTTP 400 Bad Request status</li>
             * </ul>
             */
            @Test
            @DisplayName("Mismatched student-teacher returns 400")
            void submitBragLog_MismatchedRelationship_ReturnsBadRequest() {
                when(bragLogService.submitBragLog(bragLogRequest))
                        .thenThrow(new IllegalArgumentException("Teacher does not teach this student"));
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> bragLogController.submitBragLog(bragLogRequest));
                assertEquals("Teacher does not teach this student", exception.getMessage());
            }

            /**
             * Tests handling of empty behaviors.
             * <p>Verifies:
             * <ul>
             *     <li>Service exception is propagated</li>
             *     <li>HTTP 400 Bad Request status</li>
             * </ul>
             */
            @Test
            @DisplayName("Empty behaviors returns 400")
            void submitBragLog_EmptyBehaviors_ReturnsBadRequest() {
                when(bragLogService.submitBragLog(bragLogRequest))
                        .thenThrow(new IllegalArgumentException("At least one behavior must be selected"));
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> bragLogController.submitBragLog(bragLogRequest));
                assertEquals("At least one behavior must be selected", exception.getMessage());
            }

            /**
             * Tests handling of invalid behavior IDs.
             * <p>Verifies:
             * <ul>
             *     <li>Service exception is propagated</li>
             *     <li>HTTP 400 Bad Request status</li>
             * </ul>
             */
            @Test
            @DisplayName("Invalid behavior ID returns 400")
            void submitBragLog_InvalidBehavior_ReturnsBadRequest() {
                when(bragLogService.submitBragLog(bragLogRequest))
                        .thenThrow(new IllegalArgumentException("Invalid behavior ID: 999"));
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> bragLogController.submitBragLog(bragLogRequest));
                assertEquals("Invalid behavior ID: 999", exception.getMessage());
            }
        }
    }
}
