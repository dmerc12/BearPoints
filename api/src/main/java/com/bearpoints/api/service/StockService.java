package com.bearpoints.api.service;

import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.StockServiceImpl;

/**
 * Service interface for managing reward item stock levels.
 * <p>Provides atomic operations for incrementing and decrementing stock
 * with proper validation (non-negative amounts, sufficient stock checks).
 *
 * <p>Key features:
 * <ul>
 *     <li>Decrement stock with availability checks</li>
 *     <li>Increment stock (e.g., for reversals or restocking)</li>
 *     <li>Check if an item has sufficient stock without modifying (throws if not)</li>
 *     <li>All operations are transactional and throw specific exceptions</li>
 * </ul>
 *
 * @see StockServiceImpl
 * @version 1.1
 * @author Dylan Mercer
 */
public interface StockService {
    /**
     * Decrements the stock of a reward item by one.
     * <p>Checks that current stock is at least 1 before decrementing.
     *
     * @param itemId ID of the reward item
     * @throws ResourceNotFoundException if item not found
     * @throws InsufficientResourcesException if stock is zero
     */
    void decrementStock(Long itemId);

    /**
     * Increments the stock of a reward item by one.
     * <p>Used for reversals (e.g., when a reward is deleted or updated).
     *
     * @param itemId ID of the reward item
     * @throws ResourceNotFoundException if item not found
     */
    void incrementStock(Long itemId);

    /**
     * Checks whether a reward item has at least one unit in stock.
     * <p>Does not modify any data. Useful for pre-validation before performing to decrement.
     *
     * @param itemId ID of the reward item
     * @throws ResourceNotFoundException if item not found
     * @throws InsufficientResourcesException if stock is 0
     */
    void hasSufficientStock(Long itemId);
}
