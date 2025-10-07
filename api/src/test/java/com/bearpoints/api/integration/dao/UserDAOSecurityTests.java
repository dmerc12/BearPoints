package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseAuthFilter;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link UserDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>ADMIN role requirement for delete operations</li>
 *     <li>TEACHER role can create STUDENT users</li>
 *     <li>Public access to email lookup</li>
 *     <li>All authenticated users can access user lists</li>
 * </ul>
 *
 * @see WithMockUser
 * @version 1.1
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

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
    }

    // ============
    // CREATE TESTS
    // ============
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
    @DisplayName("TEACHER can save STUDENT users")
    void teacherShouldSaveStudentUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("Student");
        newUser.setEmail("new.student@okcps.org");
        newUser.setRole(Role.STUDENT);
        assertDoesNotThrow(() -> userDAO.save(newUser));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot save TEACHER users")
    void teacherShouldNotSaveTeacherUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("Teacher");
        newUser.setEmail("new.teacher@okcps.org");
        newUser.setRole(Role.TEACHER);
        assertThrows(AccessDeniedException.class, () -> userDAO.save(newUser));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot save ADMIN users")
    void teacherShouldNotSaveAdminUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("Admin");
        newUser.setEmail("new.admin@okcps.org");
        newUser.setRole(Role.ADMIN);
        assertThrows(AccessDeniedException.class, () -> userDAO.save(newUser));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot save users")
    void studentShouldNotSaveUsers() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("Student");
        newUser.setEmail("new.student@okcps.org");
        newUser.setRole(Role.STUDENT);
        assertThrows(AccessDeniedException.class, () -> userDAO.save(newUser));
    }

    // ==========
    // READ TESTS
    // ==========
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can access all users")
    void adminCanAccessAllUsers() {
        User testUser = new User();
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setEmail("admin@okcps.org");
        testUser.setRole(Role.ADMIN);
        entityManager.persist(testUser);
        entityManager.flush();
        assertDoesNotThrow(() -> {
            List<User> result = userDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can access all users")
    void teacherCanAccessAllUsers() {
        User testUser = new User();
        testUser.setFirstName("Teacher");
        testUser.setLastName("User");
        testUser.setEmail("teacher@okcps.org");
        testUser.setRole(Role.TEACHER);
        entityManager.persist(testUser);
        entityManager.flush();
        assertDoesNotThrow(() -> {
            List<User> result = userDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can access all users")
    void studentCanAccessAllUsers() {
        User testUser = new User();
        testUser.setFirstName("STUDENT");
        testUser.setLastName("User");
        testUser.setEmail("student@okcps.org");
        testUser.setRole(Role.STUDENT);
        entityManager.persist(testUser);
        entityManager.flush();
        assertDoesNotThrow(() -> {
            List<User> result = userDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    // ============
    // DELETE TESTS
    // ============
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
    @DisplayName("TEACHER cannot delete users")
    void teacherCannotDeleteUsers() {
        User user = new User();
        user.setFirstName("Temp");
        user.setLastName("User");
        user.setEmail("temp.user@okcps.org");
        user.setRole(Role.STUDENT);
        entityManager.persist(user);
        entityManager.flush();
        assertThrows(AccessDeniedException.class, () -> userDAO.delete(user));
    }
}
