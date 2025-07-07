package com.bearpoints.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * Global exception handler for REST controllers.
 * <p>This class provides centralized exception handling across all controller methods using
 * Spring's {@code @RestControllerAdvice}. It intercepts exceptions thrown during request processing
 * and maps them to appropriate HTTP responses.
 *
 * <p>Current exception handlers:
 * <ul>
 *     <li>{@link IllegalArgumentException} - Returns HTTP 400 Bad Request</li>
 *     <li>{@link IOException} - Returns HTTP 500 Internal Server Error</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @version 1.0
 * @author Dylan Mercer
 */
@RestControllerAdvice
public class GlobalHandlers {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlers.class);

    /**
     * Handles illegal argument exceptions.
     * <p>Returns a 400 Bad Request status with the exception message in the response body.
     *
     * @param ex The caught IllegalArgumentException
     * @return ResponseEntity with error message and status code
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Client sent invalid request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Handles IO exceptions.
     * <p>Returns a 500 Internal Server Error status with a generic error message.
     * Logs the full exception details internally while avoiding exposure of sensitive details.
     *
     * @param ex The caught IOException
     * @return ResponseEntity with generic error message and status code
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        logger.error("IO Exception occurred", ex);
        return ResponseEntity.internalServerError().body("Internal server error");
    }
}
