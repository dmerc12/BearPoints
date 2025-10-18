package com.bearpoints.api.unit.exception;

import com.bearpoints.api.dto.ErrorResponseDTO;
import com.bearpoints.api.exception.GlobalHandlers;
import com.bearpoints.api.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link GlobalHandlers} functionality.
 * <p>Tests verify:
 * <ul>
 *     <li>Proper HTTP status codes and messages for handled exceptions</li>
 *     <li>Correct exception type mapping</li>
 * </ul>
 *
 * @see GlobalHandlers
 * @version 1.1
 * @author Dylan Mercer
 */
public class GlobalHandlersTests {
    private final GlobalHandlers globalHandlers = new GlobalHandlers();

    @Test
    @DisplayName("Handle UserNotFoundException - returns 404 with message")
    void handleUserNotFoundException() {
        String errorMessage = "User not found with ID: 123";
        UserNotFoundException ex = new UserNotFoundException(errorMessage);
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
}
