package com.bearpoints.api.dao;

import com.bearpoints.api.entity.RewardItem;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "reward-items")
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> {
    @PreAuthorize("permitAll()")
    List<RewardItem> findAllByOrderByNameAsc();

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

    @RestResource(exported = false)
    List<RewardItem> findBySyncedToSheetsFalse();
}
