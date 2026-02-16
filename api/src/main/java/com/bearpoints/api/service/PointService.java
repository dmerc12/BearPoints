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
 *     <li>Check if a student has sufficient points without modifying (throws if not)</li>
 *     <li>All operations are transactional and throw specific exceptions</li>
 * </ul>
 *
 * @see PointServiceImpl
 * @version 1.1
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

    /**
     * Checks whether a student has at least the required number of points
     * <p>Does not modify any data. Useful for pre-validation before performing a mutation.
     *
     * @param studentId ID of the student
     * @param requiredPoints points needed (must be non-negative)
     * @throws ResourceNotFoundException if student not found
     * @throws InsufficientResourcesException if student does not have enough points
     * @throws IllegalArgumentException if requiredPoints is negative
     */
    void hasSufficientPoints(Long studentId, int requiredPoints);
}
