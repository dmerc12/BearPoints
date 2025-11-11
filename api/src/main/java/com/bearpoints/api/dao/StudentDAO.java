package com.bearpoints.api.dao;

import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.Student;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Student} entities.
 * <p>Provides CRUD operations and custom queries for student management.
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
public interface StudentDAO extends JpaRepository<Student, Long> {
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
     * Finds students by assigned teacher with pagination support.
     *
     * @param teacher Teacher entity to filter by
     * @param pageable Pagination information
     * @return Paginated list of students assigned to the teacher
     */
    Page<Student> findByTeacher(@Param("teacher") Teacher teacher, Pageable pageable);

    /**
     * Finds students by teacher ID with pagination support.
     *
     * @param teacherId Teacher ID to filter by
     * @param pageable Pagination information
     * @return Paginated list of students assigned to the teacher
     */
    Page<Student> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    /**
     * Finds students with points less than or equal to specified value.
     *
     * @param points Maximum points threshold
     * @param pageable Pagination information
     * @return Paginated list of students meeting points criteria
     */
    Page<Student> findByPointsLessThanEqual(@Param("points") Integer points, Pageable pageable);

    /**
     * Finds students by user email containing string with pagination support.
     * <p>Case-insensitive search for student management.
     *
     * @param email Email fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByUserEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);

    /**
     * Finds students by user first name containing string with pagination support.
     * <p>Case-insensitive search for student management.
     *
     * @param firstName First name fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByUserFirstNameContainingIgnoreCase(@Param("firstName") String firstName, Pageable pageable);

    /**
     * Finds students by user last name containing string with pagination support.
     * <p>Case-insensitive search for student management.
     *
     * @param lastName Last name fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByUserLastNameContainingIgnoreCase(@Param("lastName") String lastName, Pageable pageable);

    /**
     * Finds students by teacher and user email containing string with pagination support.
     * <p>Combined filter for classroom and email search.
     *
     * @param teacher Teacher to filter by
     * @param email Email fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByTeacherAndUserEmailContainingIgnoreCase(@Param("teacher") Teacher teacher,
                                                                @Param("email") String email,
                                                                Pageable pageable);
    /**
     * Finds students by teacher and user first name containing string with pagination support.
     * <p>Combined filter for classroom and first name search.
     *
     * @param teacher Teacher to filter by
     * @param firstName First name fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByTeacherAndUserFirstNameContainingIgnoreCase(@Param("teacher") Teacher teacher,
                                                                @Param("firstName") String firstName,
                                                                    Pageable pageable);

    /**
     * Finds students by teacher and user last name containing string with pagination support.
     * <p>Combined filter for classroom and last name search.
     *
     * @param teacher Teacher to filter by
     * @param lastName Last name fragment to search for
     * @param pageable Pagination information
     * @return Paginated list of matching students
     */
    Page<Student> findByTeacherAndUserLastNameContainingIgnoreCase(@Param("teacher") Teacher teacher,
                                                                    @Param("lastName") String lastName,
                                                                    Pageable pageable);

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
     * Counts students by teacher.
     *
     * @param teacher Teacher to count students for
     * @return Number of students assigned to the teacher
     */
    long countByTeacher(@Param("teacher") Teacher teacher);

    /**
     * Finds students by points range with pagination support.
     *
     * @param minPoints Minimum points (inclusive)
     * @param maxPoints Maximum points (inclusive)
     * @param pageable Pagination information
     * @return Paginated list of students within points range
     */
    Page<Student> findByPointsBetween(@Param("minPoints") Integer minPoints,
                                      @Param("maxPoints") Integer maxPoints,
                                      Pageable pageable);

    /**
     * Finds students ordered by points descending with pagination.
     * <p>Useful for leaderboards and ranking.
     *
     * @param pageable Pagination information
     * @return Paginated list of students ordered by points (highest first)
     */
    Page<Student> findByOrderByPointsDesc(Pageable pageable);

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
