package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.*;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.UserNotFoundException;
import com.bearpoints.api.service.AdminService;
import com.bearpoints.api.specification.AdminSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link AdminService} for administrative user management.
 * <p>Handles admin-specific user operations without security concerns.
 * Security is handled at the controller level.
 *
 * @see AdminService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final UserDAO userDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> getAllAdmins(Pageable pageable) {
        log.debug("Retrieving all admin users with pagination: {}", pageable);
        Page<UserDTO> adminPage = userDAO.findByRole(Role.ADMIN, pageable)
                .map(UserDTO::new);
        log.info("Retrieved {} admin users out of {} total",
                adminPage.getNumberOfElements(),
                adminPage.getTotalElements());
        return PagedResponseDTO.of(adminPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> searchAdmins(AdminSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching admins with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided, return all admins
            return getAllAdmins(pageable);
        }
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<UserDTO> adminPage = userDAO.findAll(spec, pageable).map(UserDTO::new);
        log.info("Found {} admins matching search criteria", adminPage.getNumberOfElements());
        return PagedResponseDTO.of(adminPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> searchAdminsByEmail(String email, Pageable pageable) {
        log.debug("Searching admin users by email: {} with pagination: {}", email, pageable);
        Page<UserDTO> adminPage = userDAO.findByRoleAndEmailContainingIgnoreCase(Role.ADMIN, email, pageable)
                .map(UserDTO::new);
        log.info("Found {} admin users matching email '{}' out of {} total",
                adminPage.getNumberOfElements(),
                email,
                adminPage.getTotalElements());
        return PagedResponseDTO.of(adminPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> searchAdminsByFirstName(String firstName, Pageable pageable) {
        log.debug("Searching admin users by first name: {} with pagination {}", firstName, pageable);
        Page<UserDTO> adminPage = userDAO.findByRoleAndFirstNameContainingIgnoreCase(Role.ADMIN, firstName, pageable)
                .map(UserDTO::new);
        log.info("Found {} admin users matching first name '{}' out of {} total",
                adminPage.getNumberOfElements(),
                firstName,
                adminPage.getTotalElements());
        return PagedResponseDTO.of(adminPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> searchAdminsByLastName(String lastName, Pageable pageable) {
        log.debug("Searching admin users by last name: {} with pagination: {}", lastName, pageable);
        Page<UserDTO> adminPage = userDAO.findByRoleAndLastNameContainingIgnoreCase(Role.ADMIN, lastName, pageable)
                .map(UserDTO::new);
        log.info("Found {} admin users matching last name '{}' out of {} total",
                adminPage.getNumberOfElements(),
                lastName,
                adminPage.getTotalElements());
        return PagedResponseDTO.of(adminPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDTO getAdminById(Long id) {
        log.debug("Retrieving admin user by ID: {}", id);
        User admin = userDAO.findById(id)
                .filter(user -> user.getRole() == Role.ADMIN)
                .orElseThrow(() -> new UserNotFoundException("Administrator not found with ID: " + id));
        log.debug("Successfully retrieved admin user ID: {}", id);
        return new UserDTO(admin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDTO createAdmin(UserDTO userDTO) {
        log.debug("Creating new admin user with email: {}", userDTO.getEmail());
        Optional<User> existingEmail = userDAO.findByEmail(userDTO.getEmail());
        if (existingEmail.isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User admin = new User();
        admin.setEmail(userDTO.getEmail());
        admin.setFirstName(userDTO.getFirstName());
        admin.setLastName(userDTO.getLastName());
        admin.setRole(Role.ADMIN);
        admin.setSyncedToSheets(false);
        User savedAdmin = userDAO.save(admin);
        log.info("Successfully created admin user with ID: {}", savedAdmin.getId());
        return new UserDTO(savedAdmin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDTO updateAdmin(Long id, UserDTO userDTO) {
        log.debug("Updating admin user ID: {}", id);
        User admin = userDAO.findById(id)
                .filter(user -> user.getRole() == Role.ADMIN)
                .orElseThrow(() -> new UserNotFoundException("Administrator not found with ID: " + id));
        if (!admin.getEmail().equals(userDTO.getEmail())) {
            Optional<User> existingEmail = userDAO.findByEmail(userDTO.getEmail());
            if (existingEmail.isPresent()) {
                throw new DuplicateResourceException("A user with this email already exists");
            }
        }
        admin.setEmail(userDTO.getEmail());
        admin.setFirstName(userDTO.getFirstName());
        admin.setLastName(userDTO.getLastName());
        User updatedAdmin = userDAO.save(admin);
        log.info("Successfully updated admin user ID: {}", id);
        return new UserDTO(updatedAdmin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAdmin(Long id) {
        log.debug("Deleting admin user with ID: {}", id);
        User admin = userDAO.findById(id)
                .filter(user -> user.getRole() == Role.ADMIN)
                .orElseThrow(() -> new UserNotFoundException("Administrator not found with ID: " + id));
        userDAO.delete(admin);
        log.info("Successfully deleted admin user with ID: {}", id);
    }
}
