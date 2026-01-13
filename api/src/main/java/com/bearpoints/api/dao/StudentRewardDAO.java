package com.bearpoints.api.dao;

import com.bearpoints.api.entity.StudentReward;
import io.micrometer.common.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * JPA repository for {@link StudentReward} entities.
 * <p>Provides CRUD operations and queries for student reward management
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * @see StudentReward
 * @version 2.0
 * @author Dylan Mercer
 */
public interface StudentRewardDAO extends JpaRepository<StudentReward, Long>, JpaSpecificationExecutor<StudentReward> {
    /**
     * Retrieves all student rewards with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return Paginated list of all student rewards
     */
    @NonNull
    @Override
    @Cacheable("studentRewards")
    Page<StudentReward> findAll(@NonNull Pageable pageable);

    /**
     * Retrieves all student rewards with pagination and caching support.
     *
     * @param spec Specification to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of all student rewards
     */
    @NonNull
    @Override
    @Cacheable("studentRewards")
    Page<StudentReward> findAll(@Nullable Specification<StudentReward> spec, @NonNull Pageable pageable);

    /**
     * Finds un-synced student rewards (internal use only).
     *
     * @return List of unsynced student rewards
     */
    List<StudentReward> findBySyncedToSheetsFalse();
}
