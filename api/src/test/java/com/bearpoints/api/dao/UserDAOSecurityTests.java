package com.bearpoints.api.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link UserDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>ADMIN role requirement for write operations</li>
 *     <li>Public access to email lookup</li>
 *     <li>ADMIN-only access for delete operations</li>
 * </ul>
 *
 * @see WithMockUser
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, UserDAOSecurityTests.TestConfig.class})
public class UserDAOSecurityTests {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private TestEntityManager entityManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can save users")
    void adminShouldSaveUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@okcps.org");
        newUser.setRole(Role.STUDENT);
        assertDoesNotThrow(() -> userDAO.save(newUser));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot save users")
    void nonAdminShouldNotSaveUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@okcps.org");
        newUser.setRole(Role.STUDENT);
        assertThrows(AccessDeniedException.class, () -> userDAO.save(newUser));
    }

    @Test
    @DisplayName("Unauthenticated user can find by email")
    void unauthenticatedUserShouldFindByEmail() {
        User publicUser = new User();
        publicUser.setFirstName("Public");
        publicUser.setLastName("User");
        publicUser.setEmail("public.user@okcps.org");
        publicUser.setRole(Role.STUDENT);
        entityManager.persist(publicUser);
        entityManager.flush();
        assertDoesNotThrow(() -> {
            Optional<User> result = userDAO.findByEmail(publicUser.getEmail());
            assertTrue(result.isPresent());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot delete users")
    void studentShouldNotDeleteUsers() {
        User user = new User();
        user.setFirstName("Temp");
        user.setLastName("User");
        user.setEmail("temp.user@okcps.org");
        user.setRole(Role.STUDENT);
        entityManager.persist(user);
        entityManager.flush();
        assertThrows(AccessDeniedException.class, () -> userDAO.delete(user));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot access all users")
    void nonAdminShouldNotAccessAllUsers() {
        assertThrows(AccessDeniedException.class, () -> userDAO.findAll());
    }
}
