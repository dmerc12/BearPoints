package com.bearpoints.api.service;

import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.exception.UserNotFoundException;
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
     * Searches teacher users by email with pagination and sorting.
     *
     * @param email Email search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching teacher DTOs
     */
    PagedResponseDTO<TeacherDTO> searchTeachersByEmail(String email, Pageable pageable);

    /**
     * Searches teacher users by first name with pagination and sorting.
     *
     * @param firstName First name search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching teacher DTOs
     */
    PagedResponseDTO<TeacherDTO> searchTeachersByFirstName(String firstName, Pageable pageable);

    /**
     * Searches teacher users by last name with pagination and sorting.
     *
     * @param lastName Last name search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching teacher DTOs
     */
    PagedResponseDTO<TeacherDTO> searchTeachersByLastName(String lastName, Pageable pageable);

    /**
     * Searches teacher users by grade level with pagination and sorting.
     *
     * @param grade Grade level search term
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching teacher DTOs
     */
    PagedResponseDTO<TeacherDTO> searchTeachersByGrade(GradeLevel grade, Pageable pageable);

    /**
     * Retrieves a teacher by ID.
     *
     * @param id ID of the teacher to retrieve
     * @return Teacher user DTO
     * @throws UserNotFoundException if teacher not found
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
     * @throws UserNotFoundException if teacher not found
     */
    void deleteTeacher(Long id);
}
