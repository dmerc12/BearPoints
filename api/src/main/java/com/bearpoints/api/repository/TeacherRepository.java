package com.bearpoints.api.repository;

import com.bearpoints.api.domain.Teacher;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "teachers")
public interface TeacherRepository  extends JpaRepository<Teacher, Long> {
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Teacher> findByUserEmail(String email);

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Teacher> findByGrade(String email);

    @NonNull
    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    <S extends Teacher> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull Teacher entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends Teacher> entities);

    @RestResource(exported = false)
    List<Teacher> findBySyncedToSheetsFalse();
}
