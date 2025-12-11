package com.bearpoints.api.dao;

import com.bearpoints.api.projection.StudentRewardProjection;
import com.bearpoints.api.entity.StudentReward;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * JPA repository for {@link StudentReward} entities.
 * <p>Manages reward redemption records with synchronization capabilities.
 * Exposes REST endpoints under '/student-rewards' with role-based access control.
 *
 * <p>Key features:
 * <ul>
 *     <li>Student reward redemption tracking</li>
 *     <li>Internal synchronization for Google Sheets integration</li>
 *     <li>Role-based access control for write operations</li>
 *     <li>Uses {@link StudentRewardProjection} for condensed REST representations</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Save: Requires STUDENT, TEACHER, or ADMIN role</li>
 *     <li>Delete: ADMIN role only</li>
 *     <li>Sync methods: Internal use only</li>
 * </ul>
 *
 * <p>Usage notes:
 * <ul>
 *     <li>Students can redeem rewards via save operations</li>
 *     <li>Teachers can record student reward redemptions</li>
 *     <li>Sync methods used for Google Sheets integration</li>
 * </ul>
 *
 * <p>Projection Usage:
 * REST representations use {@link StudentRewardProjection} by default for condensed views.
 *
 * @see StudentReward
 * @see StudentRewardProjection
 * @version 1.1
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "student-rewards",
        excerptProjection = StudentRewardProjection.class
)
public interface StudentRewardDAO extends JpaRepository<StudentReward, Long> {
    /**
     * Saves a student reward redemption record.
     * <p>Requires STUDENT, TEACHER, or ADMIN role. Used for reward redemption.
     *
     * @param entity StudentReward to save
     * @return Saved student reward record
     */
    @NonNull
    @Override
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    <S extends StudentReward> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull StudentReward entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends StudentReward> entities);

    /**
     * Finds un-synced student rewards (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced student rewards
     */
    @RestResource(exported = false)
    List<StudentReward> findBySyncedToSheetsFalse();
}
