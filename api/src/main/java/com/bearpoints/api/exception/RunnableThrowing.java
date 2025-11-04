package com.bearpoints.api.exception;

import java.io.IOException;

/**
 * Functional interface representing a runnable operation that can throw an IOException.
 * <p>Similar to {@link java.lang.Runnable} but allows checked IOExceptions to be thrown.
 * Used primarily for operations involving I/O like Google Sheets API calls.
 *
 * @see java.lang.Runnable
 * @version 1.0
 * @author Dylan Mercer
 */
@FunctionalInterface
public interface RunnableThrowing {
    /**
     * Executes the operation, potentially throwing an IOException.
     *
     * @throws IOException if an I/O error occurs during execution
     */
    void run() throws IOException;
}
