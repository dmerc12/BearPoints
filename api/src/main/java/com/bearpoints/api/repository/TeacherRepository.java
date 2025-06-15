package com.bearpoints.api.repository;

import com.bearpoints.api.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RepositoryRestResource(path = "teachers")
public interface TeacherRepository  extends JpaRepository<Teacher, Long> {
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Teacher> findByUserEmail(String email);

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Teacher> findByGrade(String email);

    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    <S extends Teacher> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(Teacher entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends Teacher> entities);
}
