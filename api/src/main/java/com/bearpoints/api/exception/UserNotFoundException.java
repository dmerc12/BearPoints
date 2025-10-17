package com.bearpoints.api.exception;

/**
 * Exception thrown when a user cannot be found in the system.
 * <p>Used to indicate resource not found scenarios in user management operations.
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
