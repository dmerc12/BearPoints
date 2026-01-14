package com.bearpoints.api.service;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentRewardDTO;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for student reward management operations.
 * <p>Provides paginated student reward retrieval with filtering and sorting.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated student reward retrieval with sorting</li>
 *     <li>Student reward search functionality</li>
 *     <li>Basic student reward CRUD operations</li>
 *     <li>Variety of filtering methods</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface StudentRewardService {
    /**
     * Retrieves all student rewards with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of student reward DTOs
     */
    PagedResponseDTO<StudentRewardDTO> getAllStudentRewards(Pageable pageable);

    /**
     * Searches student rewards by any field (studentName, student ID, itemName, item ID, minPointsUsed, maxPointsUsed,
     * startDate, endDate) with pagination and sorting
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of matching student reward DTOs
     */
    PagedResponseDTO<StudentRewardDTO> searchStudentRewards(StudentRewardSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a student reward by ID.
     *
     * @param id ID of the student reward to retrieve
     * @return Student Reward DTO
     * @throws ResourceNotFoundException if student reward not found
     */
    StudentRewardDTO getStudentRewardById(Long id);

    /**
     * Creates a student reward.
     *
     * @param studentRewardDTO Student Reward data to create
     * @return Created student reward DTO
     */
    StudentRewardDTO createStudentReward(StudentRewardDTO studentRewardDTO);

    /**
     * Updates an existing student reward.
     *
     * @param id ID of the student reward to update
     * @param studentRewardDTO Updated student reward data
     * @return Updated student reward DTO
     * @throws ResourceNotFoundException if student reward not found
     */
    StudentRewardDTO updateStudentReward(Long id, StudentRewardDTO studentRewardDTO);

    /**
     * Deletes a student reward by ID.
     *
     * @param id Student Reward ID to delete
     * @throws ResourceNotFoundException if student reward not found
     */
    void deleteStudentReward(Long id);
}
