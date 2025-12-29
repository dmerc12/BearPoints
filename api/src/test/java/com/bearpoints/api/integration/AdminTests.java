package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.AdminController;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link AdminController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete administrative user management flow from HTTP endpoint through service layer to
 * database, validating system behavior against a production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive admin data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 1.2
 * @author Dylan Mercer
 */
@DisplayName("Admin Integration Tests")
public class AdminTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDAO userDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/admins";
    }

    @Nested
    @DisplayName("GET /admins - Retrieve admin users")
    class GetAllAdmins {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns paginated admin users with default parameters")
        void returnsPaginatedAdminsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedAdmins() throws Exception {
            mockMvc.perform(get(baseUrl)
                        .param("sort", "firstName,asc;email,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].firstName").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns empty page when no admins exist")
        void returnsEmptyPageWhenNoAdmins() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /admins/search - Search admin users")
    class SearchAdmins {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by email returns matching admins")
        void searchByEmail_returnsMatchingAdmins() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].email",
                            everyItem(containsStringIgnoringCase("admin"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by first name returns matching admins")
        void searchByFirstName_returnsMatchingAdmins() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("firstName", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].firstName",
                            everyItem(containsStringIgnoringCase("admin"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by last name returns matching admins")
        void searchByLastName_returnsMatchingAdmins() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("with empty criteria returns all admins")
        void searchWithEmptyCriteria_returnsAllAdmins() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_returnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "nonexistentemail@okcps.org"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("with combined criteria returns matching admins")
        void searchWithCombinedCriteria_returnsMatchingAdmins() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "admin")
                            .param("firstName", "admin")
                            .param("lastName", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].email",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].firstName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "admin")
                            .param("sort", "lastName,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))));
        }
    }

    @Nested
    @DisplayName("GET /admins/{id} - Get admin by ID")
    class GetAdminById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns admin when ID exists and user is admin")
        void returnsAdmin_whenIdExists() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long adminId = adminUser.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", adminId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(adminId))
                        .andExpect(jsonPath("$.role").value("ADMIN"));
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(get(baseUrl + "/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Administrator not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns 404 when ID belongs to a student")
        void returns404_whenIdIsStudent() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", studentId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + studentId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns 404 when ID belongs to a teacher")
        void returns404_whenIdIsTeacher() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long teacherId = teacherUser.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", teacherId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + teacherId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }
    }

    @Nested
    @DisplayName("POST /admins - Create admin")
    class CreateAdmin {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("creates admin with valid data")
        void createdAdmin_withValidData() throws Exception {
            String uniqueEmail = "unique-admin-" + System.currentTimeMillis() + "@okcps.org";
            String adminJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Admin"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with invalid email format")
        void returns400_withInvalidEmailFormat() throws Exception {
            String adminJson = """
                    {
                        "email": "invalid-email",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with invalid email domain")
        void returns400_withInvalidEmailDomain() throws Exception {
            String adminJson = """
                    {
                        "email": "test@example.com",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with duplicate email")
        void returns400_withDuplicateEmail() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                String adminEmail = adminUser.get().getEmail();
                String adminJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """.formatted(adminEmail);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminJson))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value(containsString("A user with this email already exists")));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank email")
        void returns400_withBlankEmail() throws Exception {
            String adminJson = """
                    {
                        "email": "",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank first name")
        void returns400_withBlankFirstName() throws Exception {
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "",
                        "lastName": "Admin"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.firstName")
                            .value(containsString("First name must be between 1 and 100 characters")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with first name too long")
        void returns400_withFirstNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "%s",
                        "lastName": "Admin"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.firstName")
                            .value(containsString("First name must be between 1 and 100 characters")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank last name")
        void returns400_withBlankLastName() throws Exception {
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "New",
                        "lastName": ""
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.lastName")
                            .value(containsString("Last name must be between 1 and 100 characters")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with last name too long")
        void returns400_withLastNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "New",
                        "lastName": "%s"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.lastName")
                            .value(containsString("Last name must be between 1 and 100 characters")));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER"})
        @DisplayName("returns 403 when user is not ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
            String uniqueEmail = "unique-admin-" + System.currentTimeMillis() + "@okcps.org";
            String adminJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Admin"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /admins/{id} - Update admin")
    class UpdateAdmin {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates admin with valid data")
        void updatedAdmin_withValidData() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long adminId = adminUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated.email@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(adminId))
                        .andExpect(jsonPath("$.email").value("updated.email@okcps.org"))
                        .andExpect(jsonPath("$.firstName").value("Updated"))
                        .andExpect(jsonPath("$.lastName").value("Updated"))
                        .andExpect(jsonPath("$.role").value(adminUser.get().getRole().name()));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating non-existent admin")
        void returns404_whenUpdatingNonExistentAdmin() throws Exception {
            Long nonExistentId = 9999L;
            String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated"
                }
                """;
            mockMvc.perform(put(baseUrl + "/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Administrator not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating non-admin, student user")
        void returns404_whenUpdatingNonAdminStudentUser() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + studentId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating non-admin, teacher user")
        void returns404_whenUpdatingNonAdminTeacherUser() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long teacherId = teacherUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + teacherId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT"})
        @DisplayName("returns 403 when non-admin tries to update admin")
        void returns403_whenNonAdminTriesToDelete() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long adminId = adminUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", adminId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /admins/{id} - Delete admin")
    class DeleteAdmin {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deletes admin and returns 204")
        void deletesAdmin_andReturns204() throws Exception {
            Optional<User> existingUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingUser.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", existingUser.get().getId())
                                .with(csrf()))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-existent admin")
        void returns404_whenDeletingNonExistentAdmin() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(delete(baseUrl + "/{id}", nonExistentId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Administrator not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-admin, student user")
        void returns404_whenDeletingNonAdminStudentUser() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                mockMvc.perform(delete(baseUrl + "/{id}", studentId)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + studentId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-admin, teacher user")
        void returns404_whenDeletingNonAdminTeacherUser() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long teacherId = teacherUser.get().getId();
                mockMvc.perform(delete(baseUrl + "/{id}", teacherId)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value("Administrator not found with ID: " + teacherId))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT"})
        @DisplayName("returns 403 when non-admin tries to delete")
        void returns403_whenNonAdminTriesToDelete() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
