package com.bearpoints.api.controller;

import com.bearpoints.api.dto.AdminSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
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
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName, asc)
     * @return Paginated response of admin users
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserDTO>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort
    ) {
        log.debug("Retrieving all admin users - page: {}, size: {}, sort: {}", page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
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
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName,asc)
     * @return Paginated response of matching admins
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<UserDTO>> searchAdmins(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort
    ) {
        log.debug("Searching admins - email: {}, firstName: {}, lastName: {} - page: {}, size: {}, sort: {}",
                email, firstName, lastName, page, size, sort);
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
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

    private String[] splitSortParams(String sort) {
        return sort.split(",");
    }
}
