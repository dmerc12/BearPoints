package com.bearpoints.api.dao;

import com.bearpoints.api.dto.UserProjection;
import com.bearpoints.api.entity.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
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
 *     <li>Uses {@link UserProjection} for condensed REST representations</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>ADMIN role required for all operations except email lookup</li>
 *     <li>TEACHER role can create STUDENT users</li>
 *     <li>Email lookup is publicly accessible</li>
 *     <li>All authenticated users can access user lists</li>
 * </ul>
 *
 * <p>Projection Usage:
 * REST representations use {@link UserProjection} by default for condensed views.
 *
 * @see User
 * @see UserProjection
 * @version 1.2
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "users",
        excerptProjection = UserProjection.class
)
@PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasRole('ADMIN') or " + "(hasRole('TEACHER') and #entity.role.name() == 'STUDENT')")
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

    @NonNull
    @Override
    @Cacheable("users")
    @PreAuthorize("isAuthenticated()")
    List<User> findAll();

    /**
     * Finds un-synchronized users (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced users
     */
    @RestResource(exported = false)
    List<User> findBySyncedToSheetsFalse();
}
