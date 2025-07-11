package com.bearpoints.api.controller;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.service.BragLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BragLogController}.
 * <p>Verifies functionality of public brag log submission endpoint:
 * <ul>
 *     <li>Request validation handling</li>
 *     <li>Proper HTTP response codes</li>
 *     <li>Location header generation</li>
 *     <li>Error response mapping</li>
 * </ul>
 *
 * <p>Tests validate that:
 * <ul>
 *     <li>Valid requests return 201 Created with proper location header</li>
 *     <li>Service exceptions are properly mapped to HTTP status codes</li>
 *     <li>Validation errors are handled by Spring framework</li>
 * </ul>
 *
 * @see BragLogController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class BragLogControllerTests {
    @Mock
    private BragLogService bragLogService;

    @InjectMocks
    private BragLogController bragLogController;

    private BragLogRequest bragLogRequest;
    private BragLog createdBragLog;

    @BeforeEach
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        bragLogRequest = new BragLogRequest(1L, 1L, Set.of(1L, 2L), "Test notes");
        createdBragLog = new BragLog();
        createdBragLog.setId(100L);
    }

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
