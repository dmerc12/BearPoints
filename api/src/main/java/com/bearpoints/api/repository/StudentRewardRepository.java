package com.bearpoints.api.repository;

import com.bearpoints.api.model.StudentReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "student-rewards")
public interface StudentRewardRepository extends JpaRepository<StudentReward, Long> {
    @Override
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    <S extends StudentReward> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(StudentReward entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends StudentReward> entities);
}
