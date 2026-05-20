package com.bearpoints.api.dao;

import com.bearpoints.api.entity.BragLog;
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
 * JPA repository for {@link BragLog} entities.
 * <p>Provides CRUD operations and queries for brag log management.
 *
 * <p>Key features:
 * <ul>
 *     <li>Standard CRUD operations</li>
 *     <li>Pagination and sorting support</li>
 *     <li>Advanced filtering via specifications</li>
 *     <li>Internal synchronization methods</li>
 * </ul>
 *
 * @see BragLog
 * @version 2.0
 * @author Dylan Mercer
 */
public interface BragLogDAO extends JpaRepository<BragLog, Long>, JpaSpecificationExecutor<BragLog> {
    /**
     * Retrieves all brag logs with pagination and caching support.
     *
     * @param pageable Pagination information
     * @return Paginated list of all brag logs
     */
    @NonNull
    @Override
    @Cacheable("bragLogs")
    Page<BragLog> findAll(@NonNull Pageable pageable);

    /**
     * Finds brag logs using specification with pagination.
     *
     * @param spec Specification to search / filter for
     * @param pageable Pagination information
     * @return Paginated list of brag logs matching specifications
     */
    @NonNull
    @Override
    @Cacheable("bragLogs")
    Page<BragLog> findAll(@Nullable Specification<BragLog> spec, @NonNull Pageable pageable);

    /**
     * Finds unsynced brag logs (internal use only).
     *
     * @return List of unsynced brag logs
     */
    List<BragLog> findBySyncedToSheetsFalse();
}
