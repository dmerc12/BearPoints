package com.bearpoints.api.dao;

import com.bearpoints.api.entity.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "users")
@PreAuthorize("hasRole('ADMIN')")
public interface UserDAO extends JpaRepository<User, Long> {
    @PreAuthorize("permitAll()")
    Optional<User> findByEmail(String email);

    @NonNull
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    <S extends User> S save(@NonNull S entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@NonNull User entity);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteAll(@NonNull Iterable<? extends User> entities);

    @RestResource(exported = false)
    List<User> findBySyncedToSheetsFalse();
}
