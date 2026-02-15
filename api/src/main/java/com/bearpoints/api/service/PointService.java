package com.bearpoints.api.service;

import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.service.impl.PointServiceImpl;

/**
 * Service interface for managing student point balances.
 * <p>Provides atomic operations for adding and subtracting points
 * with proper validation (non-negative amounts, insufficient balance checks).
 *
 * <p>Key features:
 * <ul>
 *     <li>Add points to a student's balance</li>
 *     <li>Subtract points with balance validation</li>
 *     <li>All operations are transactional and throw specific exceptions</li>
 * </ul>
 *
 * @see PointServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
public interface PointService {
    /**
     * Adds points to a student's balance.
     *
     * @param studentId ID of the student
     * @param points number of points to add (must be non-negative)
     * @throws ResourceNotFoundException if student not found
     * @throws IllegalArgumentException if points parameter is negative
     */
    void addPoints(Long studentId, int points);

    /**
     * Subtracts points from a student's balance.
     *
     * @param studentId ID of the student
     * @param points points number of points to subtract (must be non-negative)
     * @throws ResourceNotFoundException if student not found
     * @throws InsufficientResourcesException if student doesn't have enough points
     * @throws IllegalArgumentException if points parameter is negative
     */
    void subtractPoints(Long studentId, int points);
}
