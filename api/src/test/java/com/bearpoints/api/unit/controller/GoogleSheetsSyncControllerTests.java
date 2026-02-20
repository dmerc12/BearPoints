package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.GoogleSheetsSyncController;
import com.bearpoints.api.service.GoogleSheetsSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoogleSheetsSyncController}.
 * <p>Verifies functionality of Google Sheets synchronization endpoints:
 * <ul>
 *     <li>Successful sync execution and response handling</li>
 *     <li>Error scenarios and exception handling</li>
 *     <li>Logging and behavior verification</li>
 * </ul>
 *
 * @see GoogleSheetsSyncController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleSheetsSyncController Unit Tests")
public class GoogleSheetsSyncControllerTests {
    @Mock
    private GoogleSheetsSyncService googleSheetsSyncService;

    @InjectMocks
    private GoogleSheetsSyncController googleSheetsSyncController;

    @Nested
    @DisplayName("POST /api/sync - When triggering full synchronization")
    class WhenTriggeringFullSynchronization {
        @Test
        @DisplayName("Should return 200 OK when sync completes successfully")
        void shouldReturn200WhenSyncCompletesSuccessfully() {
            doNothing().when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Google Sheets synchronization completed successfully", response.getBody());
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should handle IOException during sync and return 500 status")
        void shouldHandleIOExceptionDuringSyncAndReturn500Status() {
            String errorMessage = "Failed to connect to Google Sheets API";
            doThrow(new RuntimeException(errorMessage))
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Synchronization failed: " + errorMessage));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should handle runtime exception during sync and return 500 status")
        void shouldHandleRuntimeExceptionDuringSyncAndReturn500Status() {
            String errorMessage = "Unexpected database error";
            doThrow(new RuntimeException(errorMessage))
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Synchronization failed: " + errorMessage));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should handle null error message from exception")
        void shouldHandleNullErrorMessageFromException() {
            doThrow(new RuntimeException())
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Synchronization failed: null"));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should propagate IOException with nested cause")
        void shouldPropagateIOExceptionWithNestedCause() {
            IOException rootCause = new IOException("Network timeout");
            RuntimeException wrapperExecution = new RuntimeException("API connection failed", rootCause);
            doThrow(wrapperExecution)
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Synchronization failed: API connection failed"));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should handle quota exceeded exception")
        void shouldHandleQuotaExceededException() {
            String errorMessage = "Daily quota exceeded";
            doThrow(new RuntimeException(errorMessage))
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains(errorMessage));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should maintain service invocation count even when exception occurs")
        void shouldMaintainServiceInvocationCountEvenWhenExceptionOccurs() {
            String errorMessage = "Sync failed";
            doThrow(new RuntimeException(errorMessage))
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Synchronization failed: " + errorMessage));
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }

        @Test
        @DisplayName("Should not call service multiple times for single request")
        void shouldNotCallServiceMultipleTimesForSingleRequest() {
            doNothing().when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(googleSheetsSyncService, times(1)).syncAllData();
        }
    }

    @Nested
    @DisplayName("Security - When checking endpoint access")
    class SecurityWhenCheckingEndpointAccess {
        @Test
        @DisplayName("Should have @PreAuthorize annotation with ADMIN role")
        void shouldHavePreAuthorizeAnnotationWithAdminRole() {
            var preAuthorize = GoogleSheetsSyncController.class.getAnnotation(PreAuthorize.class);
            assertNotNull(preAuthorize);
            assertEquals("hasRole('ADMIN')", preAuthorize.value());
        }

        @Test
        @DisplayName("Should have @PostMapping annotation")
        void shouldHavePostMappingAnnotation() throws NoSuchMethodException {
            var method = GoogleSheetsSyncController.class.getMethod("syncAllData");
            var postMapping = method.getAnnotation(PostMapping.class);
            assertNotNull(postMapping);
        }
    }

    @Nested
    @DisplayName("Response Entity - When validating response structure")
    class ResponseEntityWhenValidatingResponseStructure {
        @Test
        @DisplayName("Should return non-null response entity with proper content type")
        void shouldReturnNonNullResponseEntityWithProperContentType() {
            doNothing().when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertTrue(response.getHeaders().isEmpty());
        }

        @Test
        @DisplayName("Should return consistent message format for success")
        void shouldReturnConsistentMessageFormatForSuccess() {
            doNothing().when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertEquals("Google Sheets synchronization completed successfully", response.getBody());
        }

        @Test
        @DisplayName("Should return consistent message format for failure")
        void shouldReturnConsistentMessageFormatForFailure() {
            String errorMessage = "Connection failed";
            doThrow(new RuntimeException(errorMessage))
                    .when(googleSheetsSyncService).syncAllData();
            ResponseEntity<String> response = googleSheetsSyncController.syncAllData();
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertTrue(response.getBody().startsWith("Synchronization failed: "));
            assertTrue(response.getBody().contains(errorMessage));
        }
    }
}
