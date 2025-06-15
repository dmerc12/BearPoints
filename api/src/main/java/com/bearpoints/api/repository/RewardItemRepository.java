package com.bearpoints.api.repository;

import com.bearpoints.api.model.RewardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "reward-items")
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> {
    @PreAuthorize("permitAll()")
    List<RewardItem> findAllByOrderByNameAsc();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends RewardItem> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(RewardItem entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends RewardItem> entities);
}
