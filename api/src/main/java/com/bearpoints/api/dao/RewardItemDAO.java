package com.bearpoints.api.dao;

import com.bearpoints.api.dto.RewardItemProjection;
import com.bearpoints.api.entity.RewardItem;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * JPA repository for {@link RewardItem} entities.
 * <p>Provides CRUD operations and custom queries for reward item management.
 * Exposes REST endpoints under '/reward-items' with granular access control.
 *
 * <p>Key features:
 * <ul>
 *     <li>Authenticated read access for all roles</li>
 *     <li>Admin-only write operations</li>
 *     <li>Alphabetical ordering of items</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * <p>Security constraints:
 * <ul>
 *     <li>Read: All authenticated roles</li>
 *     <li>Write: ADMIN only</li>
 *     <li>Sync methods: Internal use only</li>
 * </ul>
 *
 * @see RewardItem
 * @version 1.1
 * @author Dylan Mercer
 */
@RepositoryRestResource(
        path = "reward-items",
        excerptProjection = RewardItemProjection.class
)
public interface RewardItemDAO extends JpaRepository<RewardItem, Long> {
    /**
     * Finds all reward items ordered alphabetically by name.
     * <p>Requires any authenticated role. Used for reward store listings.
     *
     * @return List of reward items sorted by name
     */
    @PreAuthorize("isAuthenticated()")
    List<RewardItem> findAllByOrderByNameAsc();

    /**
     * Retrieves all reward items.
     * <p>Requires any authenticated role. Used for reward management.
     *
     * @return List of all reward items
     */
    @NonNull
    @Override
    @PreAuthorize("isAuthenticated()")
    List<RewardItem> findAll();

    /**
     * Saves a reward item.
     * <p>Requires ADMIN role. Used for reward management.
     *
     * @param entity RewardItem to save
     * @return Saved reward item
     */
    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends RewardItem> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull RewardItem entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends RewardItem> entities);

    /**
     * Finds unsynced reward items (internal use).
     * <p>Not exposed via REST API. Used for Google Sheets synchronization.
     *
     * @return List of unsynced reward items
     */
    @RestResource(exported = false)
    List<RewardItem> findBySyncedToSheetsFalse();
}
