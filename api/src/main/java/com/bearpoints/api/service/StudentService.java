package com.bearpoints.api.service;

import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentDTO;
import com.bearpoints.api.criteria.StudentSearchCriteria;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for student management operations.
 * <p>Provides paginated student retrieval with filtering and sorting.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated student retrieval with sorting</li>
 *     <li>Student search functionality</li>
 *     <li>Basic student CRUD operations</li>
 *     <li>Classroom and points-based filtering</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface StudentService {

    /**
     * Retrieves all students with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of student DTOs
     */
    PagedResponseDTO<StudentDTO> getAllStudents(Pageable pageable);

    /**
     * Searches students by any field (email, first name, last name, teacher) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching student DTOs
     */
    PagedResponseDTO<StudentDTO> searchStudents(StudentSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves classroom leaderboard with pagination and sorting by points descending.
     *
     * @param teacherId Teacher ID to filter by
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of student DTOs for the specified teacher ordered by points
     */
    PagedResponseDTO<StudentDTO> getClassRoomLeaderboard(Long teacherId, Pageable pageable);

    /**
     * Retrieves a student by ID.
     *
     * @param id ID of the student to retrieve
     * @return Student DTO
     * @throws ResourceNotFoundException if student not found
     */
    StudentDTO getStudentById(Long id);

    /**
     * Retrieves a student by token.
     *
     * @param token Student's unique access token
     * @return Student DTO
     * @throws ResourceNotFoundException if student not found
     */
    StudentDTO getStudentByToken(String token);

    /**
     * Creates a new student.
     *
     * @param studentDTO Student data to create
     * @return Created student DTO
     * @throws DuplicateResourceException if email already exists
     */
    StudentDTO createStudent(StudentDTO studentDTO);

    /**
     * Updates an existing student.
     *
     * @param id ID of the student to update
     * @param studentDTO Updated student data
     * @return Updated student DTO
     * @throws ResourceNotFoundException if student not found
     * @throws DuplicateResourceException if email already exists
     */
    StudentDTO updateStudent(Long id, StudentDTO studentDTO);

    /**
     * Deletes a student by ID.
     *
     * @param id Student ID to delete
     * @throws ResourceNotFoundException if student not found
     */
    void deleteStudent(Long id);
}
