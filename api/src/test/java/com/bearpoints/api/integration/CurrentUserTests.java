package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.CurrentUserController;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link CurrentUserController}.
 * <p>Validates the {@code GET /api/users/me} endpoint under various authentication scenarios.
 * Extends {@link BaseIntegrationTest} to inherit the PostgreSQL Testcontainers environment
 * and test data initialized by {@link TestDataInitializer}.
 *
 * @see CurrentUserController
 * @see BaseIntegrationTest
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Current User Integration Tests")
public class CurrentUserTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDAO userDAO;

    /**
     * Helper method to perform the request as a given user and verify the response matches
     * that user's data.
     *
     * @param user the user to authenticate as
     */
    private void performGetCurrentUserWithUser(User user) throws Exception {
        FirebaseUserDetails principal = new FirebaseUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        mockMvc.perform(get("/api/users/me")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.role").value(user.getRole().name()));
    }

    @Test
    @DisplayName("GET /api/users/me as STUDENT returns correct user details")
    void getCurrentUserAsStudentReturnsUserDetails() throws Exception {
        User student = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("No student user found in test database"));
        performGetCurrentUserWithUser(student);
    }

    @Test
    @DisplayName("GET /api/users/me as TEACHER returns correct user details")
    void getCurrentUserAsTeacherReturnsUserDetails() throws Exception {
        User teacher = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("No teacher user found in test database"));
        performGetCurrentUserWithUser(teacher);
    }

    @Test
    @DisplayName("GET /api/users/me as ADMIN returns correct user details")
    void getCurrentUserAsAdminReturnsUserDetails() throws Exception {
        User admin = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("No admin user found in test database"));
        performGetCurrentUserWithUser(admin);
    }

    @Test
    @DisplayName("GET /api/users/me as STAFF returns correct user details")
    void getCurrentUserAsStaffReturnsUserDetails() throws Exception {
        User staff = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("No staff user found in test database"));
        performGetCurrentUserWithUser(staff);
    }

    @Test
    @DisplayName("GET /api/users/me without authentication returns 401 Unauthorized")
    void getCurrentUserWithoutAuthenticationReturns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }
}
