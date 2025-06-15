package com.bearpoints.api.repository;

import com.bearpoints.api.domain.StudentReward;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(path = "student-rewards")
public interface StudentRewardRepository extends JpaRepository<StudentReward, Long> {
    @NonNull
    @Override
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    <S extends StudentReward> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull StudentReward entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends StudentReward> entities);

    @RestResource(exported = false)
    List<StudentReward> findBySyncedToSheetsFalse();
}
