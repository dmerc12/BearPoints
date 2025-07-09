package com.bearpoints.api.dao;

import com.bearpoints.api.entity.BragLog;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@RepositoryRestResource(path = "brag-logs")
public interface BragLogDAO extends JpaRepository<BragLog, Long> {
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    List<BragLog> findBySyncedToSheetsFalse();

    @NonNull
    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
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

    @RestResource(exported = false)
    List<BragLog> findByTimestampAfter(LocalDateTime startDate);
}
