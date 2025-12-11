package com.bearpoints.api.unit.exception;

import com.bearpoints.api.dto.ErrorResponseDTO;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.GlobalHandlers;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalHandlers} functionality.
 * <p>Tests verify:
 * <ul>
 *     <li>Proper HTTP status codes and messages for handled exceptions</li>
 *     <li>Correct exception type mapping</li>
 *     <li>Duplicate resource exception handling</li>
 *     <li>JSON parsing and deserialization exception handling</li>
 * </ul>
 *
 * @see GlobalHandlers
 * @version 1.3
 * @author Dylan Mercer
 */
@DisplayName("Global Handlers Tests")
@ExtendWith(MockitoExtension.class)
public class GlobalHandlersTests {
    @InjectMocks
    private GlobalHandlers globalHandlers;

    @Test
    @DisplayName("Handle ResourceNotFoundException - returns 404 with message")
    void handleResourceNotFoundException() {
        String errorMessage = "User not found with ID: 123";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);
        ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleUserNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Handle IllegalArgumentException - returns 400 with message")
    void handleIllegalArgumentException() {
        String errorMessage = "Invalid parameter value";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);
        ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Handle IOException - returns 500 with generic message")
    void handleIOException() {
        IOException ex = new IOException("File not found");
        ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleIOException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Nested
    @DisplayName("Handle MethodArgumentNotValidException")
    class MethodArgumentNotValidExceptionTests {
        @Test
        @DisplayName("returns 400 with field errors")
        void handleMethodArgumentNotValidException() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError emailError = new FieldError("userDTO", "email", "Email must be @okcps.org domain");
            FieldError firstNameError = new FieldError("userDTO", "firstName", "First name must be between 1 and 100 characters");
            FieldError lastNameError = new FieldError("userDTO", "lastName", "Last name must be between 1 and 100 characters");
            List<ObjectError> allErrors = Arrays.asList(emailError, firstNameError, lastNameError);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(allErrors);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleValidationExceptions(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Validation failed", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
            Map<String, String> fieldErrors = response.getBody().getFieldErrors();
            assertNotNull(fieldErrors);
            assertEquals(3, fieldErrors.size());
            assertEquals("Email must be @okcps.org domain", fieldErrors.get("email"));
            assertEquals("First name must be between 1 and 100 characters", fieldErrors.get("firstName"));
            assertEquals("Last name must be between 1 and 100 characters", fieldErrors.get("lastName"));
        }

        @Test
        @DisplayName("with empty errors - returns 400 with empty field errors")
        void handleMethodArgumentNotValidExceptionWithEmptyErrors() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleValidationExceptions(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Validation failed", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
            Map<String, String> fieldErrors = response.getBody().getFieldErrors();
            assertNotNull(fieldErrors);
            assertTrue(fieldErrors.isEmpty());
        }
    }

    @Nested
    @DisplayName("Handle DuplicateResourceException")
    class HandleDuplicateResourceExceptionTests {
        @Test
        @DisplayName("returns 400 with message")
        void handleDuplicateResourceException() {
            String errorMessage = "A user with this email already exists";
            DuplicateResourceException ex = new DuplicateResourceException(errorMessage);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleDuplicateResourceException(ex);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with different messages")
        void handleDuplicateResourceExceptionWithDifferentMessages() {
            String[] errorMessages = {
                    "A user with this email already exists",
                    "Behavior type already exists",
                    "Resource with this identifier already exists"
            };
            for (String errorMessage: errorMessages) {
                DuplicateResourceException ex = new DuplicateResourceException(errorMessage);
                ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleDuplicateResourceException(ex);
                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(errorMessage, response.getBody().getMessage());
                assertNotNull(response.getBody().getTimestamp());
            }
        }

        @Test
        @DisplayName("with null message")
        void handleDuplicateResourceExceptionWithNullMessage() {
            DuplicateResourceException ex = new DuplicateResourceException(null);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleDuplicateResourceException(ex);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNull(response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with empty message")
        void handleDuplicateResourceExceptionWithEmptyMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("");
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleDuplicateResourceException(ex);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }
    }

    @Nested
    @DisplayName("Handle HttpMessageNotReadableException")
    class HandleHttpMessageNotReadableExceptionTests {
        @Test
        @DisplayName("with ValueInstantiationException root cause - returns 400 with root cause message")
        void handleHttpMessageNotReadableException_WithValueInstantiationException() {
            String rootCauseMessage = "Cannot construct instance of `com.bearpoints.api.dto.TeacherDTO`, problem: Invalid grade level: INVALID_GRADE";
            ValueInstantiationException rootCause = mock(ValueInstantiationException.class);
            when(rootCause.getMessage()).thenReturn(rootCauseMessage);
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(rootCause);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(rootCauseMessage, response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with InvalidFormatException root cause - returns 400 with formatted field message")
        void handleHttpMessageNotReadableException_WithInvalidFormatException() {
            InvalidFormatException rootCause = mock(InvalidFormatException.class);
            when(rootCause.getValue()).thenReturn("INVALID_ROLE");
            JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);
            when(reference.getFieldName()).thenReturn("role");
            when(rootCause.getPath()).thenReturn(Collections.singletonList(reference));
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(rootCause);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Invalid value for field 'role': INVALID_ROLE", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with IllegalArgumentException root cause - returns 400 with root cause message")
        void handleHttpMessageNotReadableException_WithIllegalArgumentException() {
            String rootCauseMessage = "Invalid grade level: INVALID_GRADE. Valid values are: [PRE_K, K, FIRST, SECOND, THIRD, FOURTH]";
            IllegalArgumentException rootCause = new IllegalArgumentException(rootCauseMessage);
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(rootCause);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(rootCauseMessage, response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with no root cause - returns 400 with default message")
        void handleHttpMessageNotReadableException_WithNoRootCause() {
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(null);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Invalid request body", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with unknown root cause type - returns 400 with default message")
        void handleHttpMessageNotReadableException_WithUnknownRootCause() {
            IOException rootCause = new IOException("Some IO error");
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(rootCause);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Invalid request body", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("with InvalidFormatException and null field name - returns 400 with fallback field name")
        void handleHttpMessageNotReadableException_WithInvalidFormatExceptionAndNullFieldName() {
            InvalidFormatException rootCause = mock(InvalidFormatException.class);
            when(rootCause.getValue()).thenReturn("INVALID_VALUE");
            JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);
            when(reference.getFieldName()).thenReturn(null);
            when(rootCause.getPath()).thenReturn(Collections.singletonList(reference));
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            when(ex.getRootCause()).thenReturn(rootCause);
            ResponseEntity<ErrorResponseDTO> response = globalHandlers.handleHttpMessageNotReadableException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Invalid value for field 'null': INVALID_VALUE", response.getBody().getMessage());
            assertNotNull(response.getBody().getTimestamp());
        }
    }
}
