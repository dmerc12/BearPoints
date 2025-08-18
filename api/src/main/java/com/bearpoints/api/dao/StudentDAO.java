package com.bearpoints.api.dao;

import com.bearpoints.api.dto.StudentSummary;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.security.SecurityUtils;
import com.bearpoints.api.entity.Student;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Student} entities with security constraints.
 * <p>Provides CRUD operations and custom queries for student management.
 * Exposes REST endpoints under '/students' with granular access control.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations with ADMIN-only delete access</li>
 *     <li>TEACHER role can manage students in their own classrooms</li>
 *     <li>Token-based public access for student self-service</li>
 *     <li>Internal synchronization methods</li>
 *     <li>Classroom-based student filtering</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Token lookup: Public access</li>
 *     <li>Email lookup: Requires TEACHER or ADMIN role</li>
 *     <li>Teacher-based lookup: Requires authenticated user</li>
 *     <li>Save operations: ADMIN or TEACHER (with classroom ownership)</li>
 *     <li>Delete operations: ADMIN only</li>
 * </ul>
 *
 * @see Student
 * @see SecurityUtils#isOwnClassroom
 * @version 1.1
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "students",
        excerptProjection = StudentSummary.class
)
public interface StudentDAO extends JpaRepository<Student, Long> {
    /**
     * Finds student by access token (public).
     * <p>Used for student self-service features without authentication.
     *
     * @param token Unique access token
     * @return Optional containing student if found
     */
    @PreAuthorize("permitAll()")
    Optional<Student> findByToken(String token);

    /**
     * Finds student by user email.
     * <p>Requires TEACHER or ADMIN role.
     *
     * @param email User's email address
     * @return Optional containing student if found
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Student> findByUserEmail(String email);

    /**
     * Finds students by assigned teacher.
     * <p>Requires any authenticated role. Used for classroom filtering.
     *
     * @param teacher Teacher entity to filter by
     * @return List of students assigned to the teacher
     */
    @PreAuthorize("isAuthenticated()")
    List<Student> findByTeacher(Teacher teacher);

    /**
     * Retrieves all students.
     * <p>Requires any authenticated role. Used for leaderboards and administration.
     *
     * @return List of all students
     */
    @NonNull
    @Override
    @PreAuthorize("isAuthenticated()")
    List<Student> findAll();

    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @securityUtils.isOwnClassroom(#entity, authentication))")
    <S extends Student> S save(@NonNull @P("entity") S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull Student entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends Student> entities);

    /**
     * Finds un-synced students (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced students
     */
    @RestResource(exported = false)
    List<Student> findBySyncedToSheetsFalse();
}
