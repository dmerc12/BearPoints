package com.bearpoints.api.service;

import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for teacher user management operations.
 * <p>Provides paginated user retrieval with filtering and sorting for TEACHER users.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated teacher user retrieval with sorting</li>
 *     <li>Filtering by email, name, and grade</li>
 *     <li>Teacher user management (create, update, delete)</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface TeacherService {
    /**
     * Retrieves all teacher users with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of teacher user DTOs
     */
    PagedResponseDTO<TeacherDTO> getAllTeachers(Pageable pageable);

    /**
     * Searches teachers by any field (email, first name, last name, grade level) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching student DTOs
     */
    PagedResponseDTO<TeacherDTO> searchTeachers(TeacherSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a teacher by ID.
     *
     * @param id ID of the teacher to retrieve
     * @return Teacher user DTO
     * @throws ResourceNotFoundException if teacher not found
     */
    TeacherDTO getTeacherById(Long id);

    /**
     * Creates a new teacher user.
     *
     * @param teacherDTO Teacher data to create (email, first name, last name, grade level)
     * @return Created teacher DTO
     * @throws DataIntegrityViolationException if email already exists
     */
    TeacherDTO createTeacher(TeacherDTO teacherDTO);

    /**
     * Updates an existing teacher user.
     *
     * @param id ID of the teacher user to update
     * @param teacherDTO Updated teacher data (email, first name, last name, grade level)
     * @return Updated teacher DTO
     * @throws DataIntegrityViolationException if email already exists
     */
    TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO);

    /**
     * Deletes a teacher by ID.
     *
     * @param id Teacher ID to delete
     * @throws ResourceNotFoundException if teacher not found
     */
    void deleteTeacher(Long id);
}
