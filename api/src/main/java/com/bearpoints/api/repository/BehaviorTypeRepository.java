package com.bearpoints.api.repository;

import com.bearpoints.api.model.BehaviorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "behavior-types")
public interface BehaviorTypeRepository extends JpaRepository<BehaviorType, Long> {
    @PreAuthorize("permitAll()")
    List<BehaviorType> findByActiveTrue();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends BehaviorType> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(BehaviorType entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends BehaviorType> entities);
}
