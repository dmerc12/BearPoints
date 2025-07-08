package com.bearpoints.api.dao;

import com.bearpoints.api.entity.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link User} entities.
 * <p>Provides CRUD operations and custom queries for user management.
 * Exposes REST endpoints under '/users' with security constraints.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations with ADMIN-only write access</li>
 *     <li>Public access to email-based user lookup</li>
 *     <li>Internal synchronization methods</li>
 *     <li>Role-based access control</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Save/delete operations require ADMIN role</li>
 *     <li>Email lookup is publicly accessible</li>
 *     <li>All other read operations require ADMIN role</li>
 * </ul>
 *
 * @see User
 * @version 1.0
 * @author Dylan Mercer
 */
@RepositoryRestResource(path = "users")
@PreAuthorize("hasRole('ADMIN')")
public interface UserDAO extends JpaRepository<User, Long> {
    /**
     * Finds a user by email address.
     * <p>Publicly accessible without authentication. Used during authentication flows.
     *
     * @param email User's email address
     * @return Optional containing the user if found
     */
    @PreAuthorize("permitAll()")
    Optional<User> findByEmail(String email);

    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends User> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull User entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends User> entities);

    /**
     * Finds un-synchronized users (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced users
     */
    @RestResource(exported = false)
    List<User> findBySyncedToSheetsFalse();
}
