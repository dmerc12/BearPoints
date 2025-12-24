package com.bearpoints.api.dao;

import com.bearpoints.api.entity.RewardItem;
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
 * JPA repository for {@link RewardItem} entities.
 * <p>Provides CRUD operations and queries for reward item management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard-CRUD operations</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * @see RewardItem
 * @version 2.0
 * @author Dylan Mercer
 */
public interface RewardItemDAO extends JpaRepository<RewardItem, Long>, JpaSpecificationExecutor<RewardItem> {
    /**
     * Retrieves all reward items with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return Paginated list of all reward items
     */
    @NonNull
    @Override
    @Cacheable("rewardItems")
    Page<RewardItem> findAll(@NonNull Pageable pageable);

    /**
     * Finds reward items using specification with pagination.
     *
     * @param spec Specification to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of all reward items
     */
    @NonNull
    @Override
    Page<RewardItem> findAll(@Nullable Specification<RewardItem> spec, @NonNull Pageable pageable);


    /**
     * Finds unsynced reward items (internal use).
     *
     * @return List of unsynced reward items
     */
    List<RewardItem> findBySyncedToSheetsFalse();

    /**
     * Finds reward item by name
     *
     * @param name Reward item name
     * @return Optional containing reward item if found
     */
    Optional<RewardItem> findByName(String name);
}
