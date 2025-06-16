package com.bearpoints.api.repository;

import com.bearpoints.api.domain.BehaviorType;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "behavior-types")
public interface BehaviorTypeRepository extends JpaRepository<BehaviorType, Long> {
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

    @RestResource(exported = false)
    List<BehaviorType> findBySyncedToSheetsFalse();

    @RestResource(exported = false)
    BehaviorType findByName(String name);
}
