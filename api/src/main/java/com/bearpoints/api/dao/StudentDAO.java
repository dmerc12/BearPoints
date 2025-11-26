package com.bearpoints.api.dao;

import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.Student;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Student} entities.
 * <p>Provides CRUD operations and queries for student management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Custom queries for student retrieval</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>

 * @see Student
 * @version 2.0
 * @author Dylan Mercer
 */
public interface StudentDAO extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    /**
     * Finds student by access token (public).
     *
     * @param token Student's unique access token
     * @return Optional containing student if found
     */
    Optional<Student> findByToken(String token);

    /**
     * Finds student by user email.
     *
     * @param email User's email address
     * @return Optional containing student if found
     */
    Optional<Student> findByUserEmail(String email);

    /**
     * Retrieves all students with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return List of all students
     */
    @NonNull
    @Override
    @Cacheable("students")
    Page<Student> findAll(@NonNull Pageable pageable);

    /**
     * Finds students using specification with pagination.
     *
     * @param spec Specifications to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of students matching specifications
     */
    @NonNull
    @Override
    Page<Student> findAll(@Nullable Specification<Student> spec, @NonNull Pageable pageable);

    /**
     * Finds students by teacher ordered by points descending with pagination.
     * <p>Useful for classroom-specific leaderboards.
     *
     * @param teacher Teacher to filter by
     * @param pageable Pagination information
     * @return Paginated list of students ordered by points (highest first)
     */
    Page<Student> findByTeacherOrderByPointsDesc(@Param("teacher") Teacher teacher, Pageable pageable);

    /**
     * Finds un-synced students (internal use only).
     * <p>Used for Google Sheets synchronization.
     *
     * @return List of unsynced students
     */
    List<Student> findBySyncedToSheetsFalse();
}
