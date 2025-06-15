package com.bearpoints.api.repository;

import com.bearpoints.api.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RepositoryRestResource(path = "students")
public interface StudentRepository extends JpaRepository<Student, Long> {
    @PreAuthorize("permitAll()")
    Optional<Student> findByToken(String token);

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    Optional<Student> findByUserEmail(String email);

    @Override
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    <S extends Student> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(Student entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends Student> entities);
}
