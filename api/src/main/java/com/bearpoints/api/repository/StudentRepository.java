package com.bearpoints.api.repository;

import com.bearpoints.api.domain.Student;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "students")
public interface StudentRepository extends JpaRepository<Student, Long> {
    @PreAuthorize("permitAll()")
    Optional<Student> findByToken(String token);

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Student> findByUserEmail(String email);

    @NonNull
    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    <S extends Student> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull Student entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends Student> entities);

    @RestResource(exported = false)
    List<Student> findBySyncedToSheetsFalse();
}
