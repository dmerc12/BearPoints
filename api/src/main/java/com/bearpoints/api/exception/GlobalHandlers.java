package com.bearpoints.api.exception;

import com.bearpoints.api.dto.ErrorResponseDTO;
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
 *     <li>{@link UserNotFoundException} - Returns HTTP 404 Not Found</li>
 *     <li>{@link IllegalArgumentException} - Returns HTTP 400 Bad Request</li>
 *     <li>{@link IOException} - Returns HTTP 500 Internal Server Error</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @version 1.1
 * @author Dylan Mercer
 */
@RestControllerAdvice
public class GlobalHandlers {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlers.class);

    /**
     * Handles User not found exceptions.
     * <p>Returns a 404 Not Found status with the exception message in the response body.
     *
     * @param ex The caught UserNotFoundException
     * @return Response entity with error message and status code
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("User not found", ex);
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDTO);
    }

    /**
     * Handles illegal argument exceptions.
     * <p>Returns a 400 Bad Request status with the exception message in the response body.
     *
     * @param ex The caught IllegalArgumentException
     * @return ResponseEntity with error message and status code
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Client sent invalid request: {}", ex.getMessage());
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponseDTO);
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
    public ResponseEntity<ErrorResponseDTO> handleIOException(IOException ex) {
        logger.error("IO Exception occurred", ex);
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("Internal server error");
        return ResponseEntity.internalServerError().body(errorResponseDTO);
    }
}
