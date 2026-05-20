package com.bearpoints.api.service;

import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.RewardItemDTO;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for reward item management operations.
 * <p>Provides paginated reward item retrieval with filtering and sorting.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated reward item retrieval with sorting</li>
 *     <li>Reward item search functionality</li>
 *     <li>Basic reward item CRUD operations</li>
 *     <li>Variety of filtering methods</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface RewardItemService {
    /**
     * Retrieves all reward items with paginated and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of reward item DTOs
     */
    PagedResponseDTO<RewardItemDTO> getAllRewardItems(Pageable pageable);

    /**
     * Searches reward items by any field (name, minPointCost, maxPointCost, minStock, maxStock)
     * with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching reward item DTOs
     */
    PagedResponseDTO<RewardItemDTO> searchRewardItems(RewardItemSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a reward item by ID.
     *
     * @param id ID of the reward item to retrieve
     * @return Reward Item DTO
     * @throws ResourceNotFoundException if reward item not found
     */
    RewardItemDTO getRewardItemById(Long id);

    /**
     * Creates a reward item.
     *
     * @param rewardItemDTO Reward Item data to create
     * @return Created reward item DTO
     * @throws DuplicateResourceException if reward item with name already exists
     */
    RewardItemDTO createRewardItem(RewardItemDTO rewardItemDTO);

    /**
     * Updates an existing reward item.
     *
     * @param id ID of the reward item to update
     * @param rewardItemDTO Updated reward item data
     * @return Updated reward item DTO
     * @throws ResourceNotFoundException if reward item not found
     * @throws DuplicateResourceException if reward item with name already exists
     */
    RewardItemDTO updateRewardItem(Long id, RewardItemDTO rewardItemDTO);

    /**
     * Deletes a reward item by ID.
     *
     * @param id Reward Item ID to delete.
     * @throws ResourceNotFoundException if reward item not found
     */
    void deleteRewardItem(Long id);
}
