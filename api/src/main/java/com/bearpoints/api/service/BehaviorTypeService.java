package com.bearpoints.api.service;

import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.BehaviorTypeSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for behavior type management operations.
 * <p>Provides paginated behavior type retrieval with filtering and sorting.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated behavior type retrieval with sorting</li>
 *     <li>Behavior type search functionality</li>
 *     <li>Basic behavior type CRUD operations</li>
 *     <li>Active and point value based filtering</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface BehaviorTypeService {

    /**
     * Retrieves all behavior types with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of behavior type DTOs
     */
    PagedResponseDTO<BehaviorTypeDTO> getAllBehaviorTypes(Pageable pageable);

    /**
     * Searches behavior types by any field (name, active, minPointValue, maxPointValue) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching behavior type DTOs
     */
    PagedResponseDTO<BehaviorTypeDTO> searchBehaviorTypes(BehaviorTypeSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a behavior type by ID.
     *
     * @param id ID of the behavior type to retrieve
     * @return Behavior Type DTO
     * @throws ResourceNotFoundException if behavior type not found
     */
    BehaviorTypeDTO getBehaviorTypeById(Long id);

    /**
     * Creates a behavior type.
     *
     * @param behaviorTypeDTO Behavior Type data to create
     * @return Created behavior type DTO
     * @throws DuplicateResourceException if name already exists
     */
    BehaviorTypeDTO createBehaviorType(BehaviorTypeDTO behaviorTypeDTO);

    /**
     * Updates an existing behavior type.
     *
     * @param id ID of the behavior type to update
     * @param behaviorTypeDTO Updated behavior type data
     * @return Updated behavior type DTO
     * @throws ResourceNotFoundException if behavior type not found
     * @throws DuplicateResourceException if name already exists
     */
    BehaviorTypeDTO updateBehaviorType(Long id, BehaviorTypeDTO behaviorTypeDTO);

    /**
     * Deletes a behavior type by ID.
     *
     * @param id Behavior Type ID to delete.
     * @throws ResourceNotFoundException if behavior type not found
     */
    void deleteBehaviorType(Long id);
}
