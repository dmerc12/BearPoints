package com.bearpoints.api.dao;

import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link User} entities.
 * <p>Provides CRUD operations and custom queries for user management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Custom queries for user retrieval</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 * </ul>

 * @see User
 * @version 2.1
 * @author Dylan Mercer
 */
public interface UserDAO extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Finds a user by email address.
     *
     * @param email User's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users by role with pagination support.
     *
     * @param role User role to filter by
     * @param pageable Pagination information
     * @return Paginated list of users with the specified role
     */
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Finds users using role and specification with pagination.
     *
     * @param spec Specifications to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of users matching role and specification
     */
    @NonNull
    Page<User> findAll(@Nullable Specification<User> spec, @NonNull Pageable pageable);

    /**
     * Retrieves all users with caching support.
     *
     * @return List of all users
     */
    @NonNull
    @Override
    @Cacheable("users")
    List<User> findAll();

    /**
     * Finds un-synchronized users (internal use only).
     * <p>Used for Google Sheets synchronization.
     *
     * @return List of unsynced users
     */
    List<User> findBySyncedToSheetsFalse();

    /**
     * Checks if user is used in any teachers or students (internal use only).
     *
     * @param userId User ID to check
     * @return true if user is used in teachers or students, false otherwise
     */
    @Query("SELECT (" +
            "SELECT COUNT(t) FROM Teacher t WHERE t.user.id = :userId) > 0 " +
            "OR " +
            "(SELECT COUNT(s) FROM Student s WHERE s.user.id = :userId) > 0")
    boolean isUserUsed(@Param("userId") Long userId);
}
