package com.bearpoints.api.dao;

import com.bearpoints.api.entity.Teacher;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Teacher} entities.
 * <p>Provides CRUD operations and custom queries for teacher management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Custom queries for teacher retrieval</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * @see Teacher
 * @version 2.0
 * @author Dylan Mercer
 */
public interface TeacherDAO extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {
    /**
     * Finds a teacher by their associated user email.
     *
     * @param email User's email address
     * @return Optional containing the teacher if found
     */
    Optional<Teacher> findByUserEmail(String email);

    /**
     * Retrieves all teachers with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return Paginated list of all teachers
     */
    @NonNull
    @Override
    @Cacheable("teachers")
    Page<Teacher> findAll(@NonNull Pageable pageable);

    /**
     * Finds teachers using specification with pagination.
     *
     * @param spec Specifications to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of teachers matching specifications
     */
    @NonNull
    @Override
    Page<Teacher> findAll(@Nullable Specification<Teacher> spec, @NonNull Pageable pageable);

    /**
     * Finds un-synced teachers (internal use only).
     * <p>Used for Google Sheets synchronization.
     *
     * @return List of unsynced teachers
     */
    List<Teacher> findBySyncedToSheetsFalse();
}
