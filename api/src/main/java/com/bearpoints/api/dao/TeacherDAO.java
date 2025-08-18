package com.bearpoints.api.dao;

import com.bearpoints.api.dto.TeacherProjection;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Teacher;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Teacher} entities.
 * <p>Provides CRUD operations and custom queries for teacher management.
 * Exposes REST endpoints under '/teachers' with security constraints.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations with ADMIN-only delete access</li>
 *     <li>Any authenticated user can access read operations</li>
 *     <li>Internal synchronization methods</li>
 *     <li>Role-based access control for write operations</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>ADMIN role required for delete operations</li>
 *     <li>ADMIN can create / update any teacher</li>
 *     <li>TEACHER can only update their own profile</li>
 *     <li>All authenticated users can access read operations</li>
 *     <li>Internal sync method not exposed via REST</li>
 * </ul>
 *
 * @see Teacher
 * @version 1.1
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "teachers",
        excerptProjection = TeacherProjection.class
)
public interface TeacherDAO extends JpaRepository<Teacher, Long> {
    /**
     * Finds a teacher by their associated user email.
     * <p>Requires any authenticated role.
     *
     * @param email User's email address
     * @return Optional containing the teacher if found
     */
    @PreAuthorize("isAuthenticated()")
    Optional<Teacher> findByUserEmail(String email);

    /**
     * Finds teachers by grade level.
     * <p>Requires any authenticated role.
     *
     * @param grade Grade level to search for
     * @return List of matching teachers
     */
    @PreAuthorize("isAuthenticated()")
    List<Teacher> findByGrade(@NotNull(message = "Grade is required") GradeLevel grade);

    /**
     * Retrieves all teachers
     * <p>Requires any authenticated role</p>
     * @return List of all teachers
     */
    @NonNull
    @Override
    @Cacheable("teachers")
    @PreAuthorize("isAuthenticated()")
    List<Teacher> findAll();

    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN') or " + "(hasRole('TEACHER') and @securityUtils.isOwnTeacher(#entity, authentication))")
    <S extends Teacher> S save(@NonNull @P("entity") S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull Teacher entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends Teacher> entities);

    /**
     * Finds un-synced teachers (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced teachers
     */
    @RestResource(exported = false)
    List<Teacher> findBySyncedToSheetsFalse();
}
