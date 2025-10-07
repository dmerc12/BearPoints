package com.bearpoints.api.unit.exception;

import com.bearpoints.api.exception.GlobalHandlers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link GlobalHandlers} functionality.
 * <p>Tests verify:
 * <ul>
 *     <li>Proper HTTP status codes and messages for handled exceptions</li>
 *     <li>Correct exception type mapping</li>
 * </ul>
 *
 * @see GlobalHandlers
 * @version 1.0
 * @author Dylan Mercer
 */
public class GlobalHandlersTests {
    private final GlobalHandlers globalHandlers = new GlobalHandlers();

    /** Test handle illegal argument exception */
    @Test
    @DisplayName("Handle IllegalArgumentException - returns 400 with message")
    void handleIllegalArgumentException() {
        String errorMessage = "Invalid parameter value";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);
        ResponseEntity<String> response = globalHandlers.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    /** Test handle IO exception */
    @Test
    @DisplayName("Handle IOException - returns 500 with generic message")
    void handleIOException() {
        IOException ex = new IOException("File not found");
        ResponseEntity<String> response = globalHandlers.handleIOException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody());
    }
}
