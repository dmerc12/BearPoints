package com.bearpoints.api.repository;

import com.bearpoints.api.model.BragLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "brag-logs")
public interface BragLogRepository extends JpaRepository<BragLog, Long> {
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    List<BragLog> findBySyncedToSheetsFalse();

    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    <S extends BragLog> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(BragLog entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends BragLog> entities);
}
