package com.bearpoints.api.dao;

import com.bearpoints.api.entity.BehaviorType;
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
 * JPA repository for {@link BehaviorType} entities.
 * <p>Provides CRUD operations and queries for behavior type management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * @see BehaviorType
 * @version 2.0
 * @author Dylan Mercer
 */
public interface BehaviorTypeDAO extends JpaRepository<BehaviorType, Long>, JpaSpecificationExecutor<BehaviorType> {
    /**
     * Retrieves all behavior types with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return List of all behavior types
     */
    @NonNull
    @Override
    @Cacheable("behaviors")
    Page<BehaviorType> findAll(@NonNull Pageable pageable);

    /**
     * Finds behavior types using specification with pagination.
     *
     * @param spec Specification to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of behavior types matching specifications
     */
    @NonNull
    @Override
    Page<BehaviorType> findAll(@Nullable Specification<BehaviorType> spec, @NonNull Pageable pageable);

    /**
     * Finds un-synchronized behavior types (internal use only).
     *
     * @return List of unsynced behavior types
     */
    List<BehaviorType> findBySyncedToSheetsFalse();

    /**
     * Finds behavior type by name.
     *
     * @param name Behavior type name
     * @return Optional containing behavior type if found
     */
    Optional<BehaviorType> findByName(String name);
}
