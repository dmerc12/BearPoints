package com.bearpoints.api.service.impl;

import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.*;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.UserService;
import com.bearpoints.api.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link UserService} for user management.
 * <p>Handles base user-specific user operations without security concerns.
 * Security is handled at the controller level.
 *
 * @see UserService
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final Set<Role> ALLOWED_ROLES = Set.of(Role.ADMIN);

    private final UserDAO userDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Retrieving all users with pagination: {}", pageable);
        Page<UserDTO> userPage = userDAO.findAll(hasAllowedRole(), pageable)
                .map(UserDTO::new);
        log.info("Retrieved {} users out of {} total",
                userPage.getNumberOfElements(),
                userPage.getTotalElements());
        return PagedResponseDTO.of(userPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<UserDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching users with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided, return all admins
            return getAllUsers(pageable);
        }
        Specification<User> spec = UserSpecification.withCriteria(criteria)
                .and(hasAllowedRole());
        Page<UserDTO> userPage = userDAO.findAll(spec, pageable).map(UserDTO::new);
        log.info("Found {} users matching search criteria", userPage.getNumberOfElements());
        return PagedResponseDTO.of(userPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDTO getUserById(Long id) {
        log.debug("Retrieving user by ID: {}", id);
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        validateIsAllowed(user);
        log.debug("Successfully retrieved user ID: {}", id);
        return new UserDTO(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.debug("Creating new user with email: {}", userDTO.getEmail());
        validateIsAllowed(userDTO);
        Optional<User> existingEmail = userDAO.findByEmail(userDTO.getEmail());
        if (existingEmail.isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setSyncedToSheets(false);
        User savedUser = userDAO.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        return new UserDTO(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.debug("Updating user ID: {}", id);
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        validateIsAllowed(user);
        if (!user.getEmail().equals(userDTO.getEmail())) {
            Optional<User> existingEmail = userDAO.findByEmail(userDTO.getEmail());
            if (existingEmail.isPresent()) {
                throw new DuplicateResourceException("A user with this email already exists");
            }
        }
        validateIsAllowed(userDTO);
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        User updatedUser = userDAO.save(user);
        log.info("Successfully updated user ID: {}", id);
        return new UserDTO(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        validateIsAllowed(user);
        userDAO.delete(user);
        log.info("Successfully deleted user with ID: {}", id);
    }

    private Specification<User> hasAllowedRole() {
        return (root, _, _) -> root.get("role").in(ALLOWED_ROLES);
    }

    private void validateIsAllowed(User user) {
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            throw new IllegalArgumentException("User service can only handle users with roles: " + ALLOWED_ROLES +
                    ", but found role: " + user.getRole());
        }
    }

    private void validateIsAllowed(UserDTO userDTO) {
        Role role = userDTO.getRole();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("User service can only handle users with roles: " + ALLOWED_ROLES +
                    ", but found role: " + role);
        }
    }
}
