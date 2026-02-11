package com.bearpoints.api.service;

import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.dto.*;
import com.bearpoints.api.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user management operations.
 * <p>Provides paginated user retrieval with filtering and sorting for users.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated user retrieval with sorting</li>
 *     <li>Filtering by email, name, and role</li>
 *     <li>User management (create, update, delete)</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
public interface UserService {
    /**
     * Retrieves all users with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of user DTOs
     */
    PagedResponseDTO<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Searches users by any field (email, first name, last name, role) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching user DTOs
     */
    PagedResponseDTO<UserDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable);

    /**
     * Retrieves a user by ID.
     *
     * @param id ID of the user to retrieve
     * @return User DTO
     * @throws ResourceNotFoundException if user not found
     */
    UserDTO getUserById(Long id);

    /**
     * Creates a new user.
     *
     * @param userDTO User data to create (email, first name, last name, role)
     * @return Created user DTO
     * @throws DataIntegrityViolationException if email already exists
     */
    UserDTO createUser(UserDTO userDTO);

    /**
     * Updates an existing user.
     *
     * @param id ID of the user to update
     * @param userDTO Updated user data (email, first name, last name, role)
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user not found
     * @throws DataIntegrityViolationException if email already exists
     */
    UserDTO updateUser(Long id, UserDTO userDTO);

    /**
     * Deletes a user by ID.
     *
     * @param id User ID to delete
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(Long id);
}
