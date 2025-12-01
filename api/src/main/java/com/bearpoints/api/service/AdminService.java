package com.bearpoints.api.service;

import com.bearpoints.api.dto.*;
import com.bearpoints.api.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for administrative user management operations.
 * <p>Provides paginated user retrieval with filtering, sorting for ADMIN users.
 *
 * <p>Key features:
 * <ul>
 *     <li>Paginated admin user retrieval with sorting</li>
 *     <li>Filtering by email, and name</li>
 *     <li>Admin user management (create, update, delete)</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface AdminService {
    /**
     * Retrieves all admin users with pagination and sorting.
     *
     * @param pageable Pagination and sorting parameters (page, size, sort)
     * @return Paginated response of admin user DTOs
     */
    PagedResponseDTO<UserDTO> getAllAdmins(Pageable pageable);

    /**
     * Searches admins by any field (email, first name, last name) with pagination and sorting.
     *
     * @param criteria Search criteria containing filters
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching admin DTOs
     */
    PagedResponseDTO<UserDTO> searchAdmins(AdminSearchCriteria criteria, Pageable pageable);

    /**
     * Searches admin users by email with pagination and sorting.
     *
     * @param email Email search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching admin user DTOs
     */
    PagedResponseDTO<UserDTO> searchAdminsByEmail(String email, Pageable pageable);

    /**
     * Searches admin users by first name with pagination and sorting.
     *
     * @param firstName First name search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching admin user DTOs
     */
    PagedResponseDTO<UserDTO> searchAdminsByFirstName(String firstName, Pageable pageable);

    /**
     * Searches admin users by last name with pagination and sorting.
     *
     * @param lastName Last name search term (case-insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Paginated response of matching admin user DTOs
     */
    PagedResponseDTO<UserDTO> searchAdminsByLastName(String lastName, Pageable pageable);

    /**
     * Retrieves an admin user by ID.
     *
     * @param id ID of the admin user to retrieve
     * @return Admin user DTO
     * @throws UserNotFoundException if admin user not found
     */
    UserDTO getAdminById(Long id);

    /**
     * Creates a new admin user.
     *
     * @param userDTO User data to create (email, first name, last name)
     * @return Created admin user DTO
     * @throws DataIntegrityViolationException if email already exists
     */
    UserDTO createAdmin(UserDTO userDTO);

    /**
     * Updates an existing admin user.
     *
     * @param id ID of the admin user to update
     * @param userDTO Updated user data (email, first name, last name)
     * @return Updated admin user DTO
     * @throws UserNotFoundException if admin user not found
     * @throws DataIntegrityViolationException if email already exists
     */
    UserDTO updateAdmin(Long id, UserDTO userDTO);

    /**
     * Deletes an admin by ID.
     *
     * @param id Admin user ID to delete
     * @throws UserNotFoundException if admin user not found
     */
    void deleteAdmin(Long id);
}
