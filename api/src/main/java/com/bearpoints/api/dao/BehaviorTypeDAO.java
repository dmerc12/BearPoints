package com.bearpoints.api.dao;

import com.bearpoints.api.dto.BehaviorTypeProjection;
import com.bearpoints.api.entity.BehaviorType;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * JPA repository for {@link BehaviorType} entities.
 * <p>Provides CRUD operations and custom queries for behavior types.
 * Exposes REST endpoints under '/behavior-types' with security constraints.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations with ADMIN-only write access</li>
 *     <li>Public access to active behavior types</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Save/delete operations require ADMIN role</li>
 *     <li>Active behavior types lists is public</li>
 * </ul>
 *
 * @see BehaviorType
 * @version 1.0
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "behavior-types",
        excerptProjection = BehaviorTypeProjection.class
)
public interface BehaviorTypeDAO extends JpaRepository<BehaviorType, Long> {
    /**
     * Finds all active behavior types.
     * <p>Publicly accessible without authentication.
     *
     * @return List of active behavior types
     */
    @PreAuthorize("permitAll()")
    List<BehaviorType> findByActiveTrue();

    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends BehaviorType> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull BehaviorType entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends BehaviorType> entities);

    /**
     * Finds un-synchronized behavior types (internal use only).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced behavior types
     */
    @RestResource(exported = false)
    List<BehaviorType> findBySyncedToSheetsFalse();

    /**
     * Finds behavior type by name (internal use only).
     * <p>Not exposed via REST API. Used during data parsing.
     *
     * @param name Behavior type name
     * @return Matching behavior type or null
     */
    @RestResource(exported = false)
    BehaviorType findByName(String name);
}
