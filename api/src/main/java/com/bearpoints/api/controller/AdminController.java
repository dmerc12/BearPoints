package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.AdminSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.service.AdminService;
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
 * REST controller for administrative user management operations.
 * <p>Provides endpoints for managing admin users with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/admins - Retrieve all admin users (any authenticated user)</li>
 *     <li>GET /api/admins/search - Search admin users with flexible criteria (any authenticated user)</li>
 *     <li>GET /api/admins/{id} - Retrieve admin user by ID (any authenticated user)</li>
 *     <li>POST /api/admins - Create new admin user (ADMIN only)</li>
 *     <li>PUT /api/admins/{id} - Update existing admin user (ADMIN only)</li>
 *     <li>DELETE /api/admins/{id} - Delete admin user (ADMIN only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN role required</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
@PreAuthorize("isAuthenticated()")
public class AdminController {
    private final AdminService adminService;

    /**
     * Retrieves all admin users with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of admin users
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserDTO>> getAllAdmins(
            @PaginationAndSorting(
                    defaultSort = "lastName,asc",
                    allowedSortProperties = {"id", "firstName", "lastName", "email"}
            ) Pageable pageable
    ) {
        log.debug("Retrieving all admin users - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<UserDTO> response = adminService.getAllAdmins(pageable);
        log.info("Retrieved {} admin users", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches admins with flexible criteria including email and name.
     * <p>Accessible to any authenticated user.
     *
     * @param email Email search term (optional)
     * @param firstName First name search term (optional)
     * @param lastName Last name search term (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching admins
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<UserDTO>> searchAdmins(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @PaginationAndSorting(
                    defaultSort = "lastName,asc",
                    allowedSortProperties = {"id", "firstName", "lastName", "email"}
            ) Pageable pageable
    ) {
        log.debug("Searching admins - email: {}, firstName: {}, lastName: {} - page: {}, size: {}, sort: {}",
                email, firstName, lastName, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        PagedResponseDTO<UserDTO> response = adminService.searchAdmins(criteria, pageable);
        log.info("Retrieved {} admins matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an admin user by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Admin user ID
     * @return Admin user details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getAdminById(@PathVariable Long id) {
        log.debug("Retrieving admin user with ID: {}", id);
        UserDTO user = adminService.getAdminById(id);
        log.info("Retrieved admin user with ID: {}", id);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new admin user.
     * <p>Accessible only to ADMIN users.
     *
     * @param userDTO Admin user data
     * @return Created admin user details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createAdmin(@Valid @RequestBody UserDTO userDTO) {
        log.debug("Creating new admin user with email: {}", userDTO.getEmail());
        UserDTO createdAdmin = adminService.createAdmin(userDTO);
        log.info("Created admin user ID: {}", createdAdmin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(createdAdmin);
    }

    /**
     * Updates an existing admin user.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Admin user ID
     * @param userDTO Updated admin user data
     * @return Updated admin user details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateAdmin(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        log.debug("Updating admin user with ID: {}", id);
        UserDTO updatedAdmin = adminService.updateAdmin(id, userDTO);
        log.info("Updated admin user ID: {}", id);
        return ResponseEntity.ok(updatedAdmin);
    }

    /**
     * Deletes an admin user by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Admin user ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> deleteAdmin(@PathVariable Long id) {
        log.debug("Deleting admin user with ID: {}", id);
        adminService.deleteAdmin(id);
        log.info("Deleted admin user with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
