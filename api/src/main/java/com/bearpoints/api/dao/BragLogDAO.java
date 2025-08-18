package com.bearpoints.api.dao;

import com.bearpoints.api.dto.BragLogProjection;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.Student;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for {@link BragLog} entities.
 * <p>Provides CRUD operations and custom queries for brag log management.
 * Exposes REST endpoints under '/brag-logs' with granular access control.
 *
 * <p>Key features:
 * <ul>
 *     <li>Public submission support via service layer</li>
 *     <li>Authenticated read access for all roles</li>
 *     <li>Internal synchronization methods</li>
 *     <li>Temporal filtering for reporting</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Create: Public via custom endpoint (/api/public/brag-logs)</li>
 *     <li>Read: All authenticated roles</li>
 *     <li>Update: ADMIN only</li>
 *     <li>Delete: ADMIN only</li>
 *     <li>Sync methods: Internal use only</li>
 * </ul>
 *
 * @see BragLog
 * @version 1.1
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "brag-logs",
        excerptProjection = BragLogProjection.class
)
public interface BragLogDAO extends JpaRepository<BragLog, Long> {
    /**
     * Finds brag logs by associated student.
     * <p>Requires any authenticated role. Used for student profiles.
     *
     * @param student Student entity
     * @return List of matching brag logs
     */
    @PreAuthorize("isAuthenticated()")
    List<BragLog> findByStudent(Student student);

    /**
     * Retrieves all brag logs.
     * <p>Requires any authenticated role. Used for leaderboards and admin views.
     *
     * @return List of all brag logs
     */
    @NonNull
    @Override
    @PreAuthorize("isAuthenticated()")
    List<BragLog> findAll();

    /**
     * Saves a brag log (public via service).
     * <p>No security constraints to allow public submission through service layer.
     * REST endpoint remains protected via HTTP security configuration
     *
     * @param entity BragLog to save
     * @return Saved brag log
     */
    @NonNull
    @Override
    <S extends BragLog> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull BragLog entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends BragLog> entities);

    /**
     * Finds unsynced brag logs (internal use).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced brag logs
     */
    @RestResource(exported = false)
    List<BragLog> findBySyncedToSheetsFalse();

    /**
     * Finds brag logs after specified timestamp.
     * <p>Internal use only. Used for reporting and analytics.
     *
     * @param startDate Starting timestamp
     * @return List of recent brag logs
     */
    @RestResource(exported = false)
    List<BragLog> findByTimestampAfter(LocalDateTime startDate);
}
