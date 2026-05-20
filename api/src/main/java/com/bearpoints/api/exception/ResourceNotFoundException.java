package com.bearpoints.api.exception;

/**
 * Exception thrown when a resource cannot be found in the system.
 * <p>Used to indicate resource not found scenarios in entity management operations.
 *
 * @version 1.1
 * @author Dylan Mercer
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
