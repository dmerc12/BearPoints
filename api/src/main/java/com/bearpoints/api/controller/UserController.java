package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management operations.
 * <p>Provides endpoints for managing users with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/users - Retrieve all users (any authenticated user)</li>
 *     <li>GET /api/users/search - Search users with flexible criteria (any authenticated user)</li>
 *     <li>GET /api/users/{id} - Retrieve user by ID (any authenticated user)</li>
 *     <li>POST /api/users - Create new user (ADMIN only)</li>
 *     <li>PUT /api/users/{id} - Update existing user (ADMIN only)</li>
 *     <li>DELETE /api/users/{id} - Delete user (ADMIN only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN role required</li>
 * </ul>
 *
 * @version 3.0
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final UserService userService;

    /**
     * Retrieves all users with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of users
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserDTO>> getAllUsers(
            @PaginationAndSorting(
                    defaultSort = "lastName,asc",
                    allowedSortProperties = {"id", "firstName", "lastName", "email", "role"}
            ) Pageable pageable
    ) {
        log.debug("Retrieving all users - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<UserDTO> response = userService.getAllUsers(pageable);
        log.info("Retrieved {} users", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches users with flexible criteria including email, name, and role.
     * <p>Accessible to any authenticated user.
     *
     * @param email Email search term (optional)
     * @param firstName First name search term (optional)
     * @param lastName Last name search term (optional)
     * @param role Role filter (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching admins
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<UserDTO>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String role,
            @PaginationAndSorting(
                    defaultSort = "lastName,asc",
                    allowedSortProperties = {"id", "firstName", "lastName", "email", "role"}
            ) Pageable pageable
    ) {
        log.debug("Searching users - email: {}, firstName: {}, lastName: {} - page: {}, size: {}, sort: {}",
                email, firstName, lastName, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        if (role != null) {
            criteria.setRole(Role.valueOf(role));
        }
        PagedResponseDTO<UserDTO> response = userService.searchUsers(criteria, pageable);
        log.info("Retrieved {} users matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a user by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.debug("Retrieving user with ID: {}", id);
        UserDTO user = userService.getUserById(id);
        log.info("Retrieved user with ID: {}", id);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user.
     * <p>Accessible only to ADMIN users.
     *
     * @param userDTO User data
     * @return Created user details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("Creating new user with email: {}", userDTO.getEmail());
        UserDTO createdUser = userService.createUser(userDTO);
        log.info("Created user ID: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(createdUser);
    }

    /**
     * Updates an existing user.
     * <p>Accessible only to ADMIN users.
     *
     * @param id User ID
     * @param userDTO Updated user data
     * @return Updated user details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        log.debug("Updating user with ID: {}", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        log.info("Updated user ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id User ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable Long id) {
        log.debug("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        log.info("Deleted user with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
