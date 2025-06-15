package com.bearpoints.api.repository;

import com.bearpoints.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RepositoryRestResource(path = "users")
@PreAuthorize("hasRole('ADMIN')")
public interface UserRepository extends JpaRepository<User, Long> {
    @PreAuthorize("permitAll()")
    Optional<User> findByEmail(String email);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends User> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(User entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(Iterable<? extends User> entities);
}
