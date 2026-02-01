package com.bearpoints.api.exception;

import com.bearpoints.api.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * <p>This class provides centralized exception handling across all controller methods using
 * Spring's {@code @RestControllerAdvice}. It intercepts exceptions thrown during request processing
 * and maps them to appropriate HTTP responses.
 *
 * <p>Current exception handlers:
 * <ul>
 *     <li>{@link ResourceNotFoundException} - Returns HTTP 404 Not Found</li>
 *     <li>{@link MethodArgumentNotValidException} - Returns HTTP 400 Bad Request with validation errors</li>
 *     <li>{@link DataIntegrityViolationException} - Returns HTTP 409 Conflict for duplicate resources</li>
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
     * Handles Resource not found exceptions.
     * <p>Returns a 404 Not Found status with the exception message in the response body.
     *
     * @param ex The caught ResourceNotFoundException
     * @return Response entity with error message and status code
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found", ex);
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDTO);
    }

    /**
     * Handles Insufficient resources exceptions.
     * <p>Returns a 400 Bad request status with the exception message in the response body.
     *
     * @param ex The caught InsufficientResourcesException
     * @return Response entity with error message and status code
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InsufficientResourcesException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientResourcesException(InsufficientResourcesException ex) {
        logger.warn("Insufficient points", ex);
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDTO);
    }

    /**
     * Handles validation exceptions from @Valid annotations.
     * <p>Returns a 400 Bad Request status with detailed field validation errors.
     *
     * @param ex The caught MethodArgumentNotValidException
     * @return Response entity with validation errors and status code
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("Validation failed", errors);
        return ResponseEntity.badRequest().body(errorResponseDTO);
    }

    /**
     * Handles data integrity violation exceptions (e.g., duplicate emails, behavior types).
     * <p>Returns a 409 Conflict status when unique constraints are violated.
     *
     * @param ex The caught DataIntegrityViolationException
     * @return Response entity with error message and status code
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DuplicateResourceException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponseDTO);
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
     * Handles JSON parsing and deserialization exceptions.
     * <p>Returns a 400 Bad Request status when JSON is malformed or contains invalid values.
     *
     * @param ex The caught HttpMessageNotReadableException
     * @return ResponseEntity with error message and status code
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("JSON parsing error: {}", ex.getMessage());
        Throwable rootCause = ex.getRootCause();
        String errorMessage = "Invalid request body";
        if (rootCause instanceof ValueInstantiationException) {
            errorMessage = rootCause.getMessage();
        } else if (rootCause instanceof InvalidFormatException ife) {
            errorMessage = "Invalid value for field '" + ife.getPath().getFirst().getFieldName() + "': " + ife.getValue();
        } else if (rootCause instanceof IllegalArgumentException) {
            errorMessage = rootCause.getMessage();
        }
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(errorMessage);
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
