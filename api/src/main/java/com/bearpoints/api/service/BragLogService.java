package com.bearpoints.api.service;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for brag log management operations.
 * <p>Provides paginated brag log retrieval with filtering and sorting.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated brag log retrieval with sorting</li>
 *     <li>Brag log search functionality</li>
 *     <li>Basic brag log CRUD operations</li>
 *     <li>Variety of filtering methods</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
public interface BragLogService {
    /**
     * Retrieves all brag logs with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of brag log DTOs
     */
    PagedResponseDTO<BragLogDTO> getAllBragLogs(Pageable pageable);

    /**
     * Searches brag logs by any field (studentName, student ID, teacherName, teacher ID, grade, minPoints, maxPoints,
     * startDate, endDate, notes) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching brag log DTOs
     */
    PagedResponseDTO<BragLogDTO> searchBragLogs(BragLogSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a brag log by ID.
     *
     * @param id ID of the brag log to retrieve
     * @return Brag Log DTO
     * @throws ResourceNotFoundException if brag log not found
     */
    BragLogDTO getBragLogById(Long id);

    /**
     * Creates a brag log.
     *
     * @param bragLogDTO Brag Log data to create
     * @return Created brag log DTO
     */
    BragLogDTO createBragLog(BragLogDTO bragLogDTO);

    /**
     * Updates an existing brag log.
     *
     * @param id ID of the brag log to update
     * @param bragLogDTO Updated brag log data
     * @return Updated brag log DTO
     * @throws ResourceNotFoundException if brag log not found
     */
    BragLogDTO updateBragLog(Long id, BragLogDTO bragLogDTO);

    /**
     * Deletes a brag log by ID.
     *
     * @param id Brag Log ID to delete
     * @throws ResourceNotFoundException if brag log not found
     */
    void deleteBragLog(Long id);

    /**
     * DEPRECATED
     * Service to assist in submitting brag logs
     * */
    BragLog submitBragLog(BragLogRequest request);
}
