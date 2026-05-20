package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.UserController;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for {@link UserController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete user management flow from HTTP endpoint through service layer to
 * database, validating system behavior against a production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 4.0
 * @author Dylan Mercer
 */
@DisplayName("User Integration Tests")
public class UserTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDAO userDAO;

    private static String baseUrl;

    private static final RequestPostProcessor WRITE_ROLES = user("admin").roles("ADMIN", "STAFF");
    private static final RequestPostProcessor DISALLOWED_ROLES = user("disallowed").roles("STUDENT", "TEACHER", "PARA");
    private static final RequestPostProcessor READ_ROLES = user("any").roles("STUDENT", "TEACHER", "ADMIN", "STAFF", "PARA");
    private static final String[] MANAGED_ROLES = {"ADMIN", "STAFF", "PARA"};

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/users";
    }


    @Nested
    @DisplayName("GET /users - Retrieve users")
    class GetAllUsers {
        @Test
        @DisplayName("returns paginated users with default parameters")
        void returnsPaginatedUsersWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedUsers() throws Exception {
            mockMvc.perform(get(baseUrl)
                        .param("sort", "firstName,asc;email,desc;role,asc")
                        .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("returns empty page when no users exist")
        void returnsEmptyPageWhenNoUsers() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /users/search - Search users")
    class SearchUsers {
        @Test
        @DisplayName("by email returns matching managed users only")
        void searchByEmail_returnsMatchingManagedUsersOnly() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "admin")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].email",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("by first name returns matching managed users")
        void searchByFirstName_returnsMatchingManagedUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("firstName", "admin")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].firstName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("by last name returns matching managed users")
        void searchByLastName_returnsMatchingManagedUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "admin")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("by ADMIN role returns matching ADMIN users")
        void searchByRole_returnsMatchingAdminUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("role", "ADMIN")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(is("ADMIN"))));
        }

        @Test
        @DisplayName("by STAFF role returns matching STAFF users")
        void searchByRole_returnsMatchingStaffUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("role", "STAFF")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(is("STAFF"))));
        }

        @Test
        @DisplayName("by PARA role returns matching PARA users")
        void searchByRole_returnsMatchingParaUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("role", "PARA")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(is("PARA"))));
        }

        @Test
        @DisplayName("with empty criteria returns all managed users (no disallowed users)")
        void searchWithEmptyCriteria_returnsAllManagedUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(in(MANAGED_ROLES))));
        }

        @Test
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_returnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "nonexistentemail@okcps.org")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("with combined criteria returns matching managed users")
        void searchWithCombinedCriteria_returnsMatchingManagedUsers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "admin")
                            .param("firstName", "admin")
                            .param("lastName", "admin")
                            .param("role", "ADMIN")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].email",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].firstName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(containsStringIgnoringCase("ADMIN"))));
        }

        @Test
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "admin")
                            .param("sort", "lastName,desc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].lastName",
                            everyItem(containsStringIgnoringCase("admin"))))
                    .andExpect(jsonPath("$.content[*].role",
                            everyItem(is("ADMIN"))));
        }

        @Test
        @DisplayName("search for TEACHER role returns empty results (not allowed)")
        void searchByTeacherRole_returnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("role", "TEACHER")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("search for STUDENT role returns empty results (not allowed)")
        void searchByStudentRole_returnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("role", "STUDENT")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /users/{id} - Get user by ID")
    class GetUserById {
        @Test
        @DisplayName("returns user when ID exists and user is ADMIN")
        void returnsUser_whenIdExistsAndAdmin() throws Exception {
            Optional<User> user = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (user.isPresent()) {
                Long userId = user.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", userId)
                                .with(READ_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.role").value("ADMIN"));
            }
        }

        @Test
        @DisplayName("returns user when ID exists and user is STAFF")
        void returnsUser_whenIdExistsAndStaff() throws Exception {
            Optional<User> user = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (user.isPresent()) {
                Long userId = user.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", userId)
                                .with(READ_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.role").value("STAFF"));
            }
        }

        @Test
        @DisplayName("returns user when ID exists and user is PARA")
        void returnsUser_whenIdExistsAndPara() throws Exception {
            Optional<User> user = userDAO.findByRole(Role.PARA, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (user.isPresent()) {
                Long userId = user.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", userId)
                                .with(READ_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.role").value("PARA"));
            }
        }

        @Test
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(get(baseUrl + "/{id}", nonExistentId)
                            .with(READ_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("User not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 when ID belongs to a student")
        void returns400_whenIdIsStudent() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", studentId)
                                .with(READ_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("STUDENT"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 when ID belongs to a teacher")
        void returns400_whenIdIsTeacher() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long teacherId = teacherUser.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", teacherId)
                                .with(READ_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("TEACHER"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }
    }

    @Nested
    @DisplayName("POST /users - Create user")
    class CreateUser {
        @Test
        @DisplayName("creates ADMIN user with valid data")
        void createdAdminUser_withValidData() throws Exception {
            String uniqueEmail = "unique-admin-" + System.currentTimeMillis() + "@okcps.org";
            String userJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .with(WRITE_ROLES))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Admin"))
                    .andExpect(jsonPath("$.role").value("ADMIN"))
                    .andExpect(jsonPath("$.teacherId").doesNotExist())
                    .andExpect(jsonPath("$.studentId").doesNotExist());
        }

        @Test
        @DisplayName("creates STAFF user with valid data")
        void createdStaffUser_withValidData() throws Exception {
            String uniqueEmail = "unique-staff-" + System.currentTimeMillis() + "@okcps.org";
            String userJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Staff",
                        "role": "STAFF"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Staff"))
                    .andExpect(jsonPath("$.role").value("STAFF"))
                    .andExpect(jsonPath("$.teacherId").doesNotExist())
                    .andExpect(jsonPath("$.studentId").doesNotExist());
        }

        @Test
        @DisplayName("creates PARA user with valid data")
        void createdParaUser_withValidData() throws Exception {
            String uniqueEmail = "unique-para-" + System.currentTimeMillis() + "@okcps.org";
            String userJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Para",
                        "role": "PARA"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Para"))
                    .andExpect(jsonPath("$.role").value("PARA"))
                    .andExpect(jsonPath("$.teacherId").doesNotExist())
                    .andExpect(jsonPath("$.studentId").doesNotExist());
        }

        @Test
        @DisplayName("returns 400 when attempting to create user with TEACHER role")
        void returns400_whenCreatingTeacher() throws Exception {
            String uniqueEmail = "unique-teacher-" + System.currentTimeMillis() + "@okcps.org";
            String userJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Teacher",
                        "role": "TEACHER"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", allOf(
                            containsString("User service can only handle users with roles:"),
                            containsString("ADMIN"),
                            containsString("STAFF"),
                            containsString("PARA"),
                            containsString(", but found role: "),
                            containsString("TEACHER"))));
        }

        @Test
        @DisplayName("returns 400 when attempting to create user with STUDENT role")
        void returns400_whenCreatingStudent() throws Exception {
            String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
            String userJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Student",
                        "role": "STUDENT"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", allOf(
                            containsString("User service can only handle users with roles:"),
                            containsString("ADMIN"),
                            containsString("STAFF"),
                            containsString("PARA"),
                            containsString(", but found role: "),
                            containsString("STUDENT"))));
        }

        @Test
        @DisplayName("returns 400 with invalid email format")
        void returns400_withInvalidEmailFormat() throws Exception {
            String adminJson = """
                    {
                        "email": "invalid-email",
                        "firstName": "New",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
        @DisplayName("returns 400 with invalid email domain")
        void returns400_withInvalidEmailDomain() throws Exception {
            String adminJson = """
                    {
                        "email": "test@example.com",
                        "firstName": "New",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
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
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """.formatted(adminEmail);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminJson)
                                .with(WRITE_ROLES))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("A user with this email already exists")));
            }
        }

        @Test
        @DisplayName("returns 400 with blank email")
        void returns400_withBlankEmail() throws Exception {
            String adminJson = """
                    {
                        "email": "",
                        "firstName": "New",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email")
                            .value(containsString("Email must be @okcps.org domain")));
        }

        @Test
        @DisplayName("returns 400 with blank first name")
        void returns400_withBlankFirstName() throws Exception {
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.firstName")
                            .value(containsString("First name must be between 1 and 100 characters")));
        }

        @Test
        @DisplayName("returns 400 with first name too long")
        void returns400_withFirstNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "%s",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.firstName")
                            .value(containsString("First name must be between 1 and 100 characters")));
        }

        @Test
        @DisplayName("returns 400 with blank last name")
        void returns400_withBlankLastName() throws Exception {
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "New",
                        "lastName": "",
                        "role": "ADMIN"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.lastName")
                            .value(containsString("Last name must be between 1 and 100 characters")));
        }

        @Test
        @DisplayName("returns 400 with last name too long")
        void returns400_withLastNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String adminJson = """
                    {
                        "email": "test@okcps.org",
                        "firstName": "New",
                        "lastName": "%s",
                        "role": "ADMIN"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.lastName")
                            .value(containsString("Last name must be between 1 and 100 characters")));
        }

        @Test
        @DisplayName("returns 403 when user is not managed user")
        void returns403_whenUserIsNoManagedUser() throws Exception {
            String uniqueEmail = "unique-admin-" + System.currentTimeMillis() + "@okcps.org";
            String adminJson = """
                    {
                        "email": "%s",
                        "firstName": "New",
                        "lastName": "Admin",
                        "role": "ADMIN"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(adminJson)
                            .with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/{id} - Update user")
    class UpdateUser {
        @Test
        @DisplayName("updates ADMIN user with valid data")
        void updatedAdminUser_withValidData() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long adminId = adminUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated.admin@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated",
                        "role": "ADMIN"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(adminId))
                        .andExpect(jsonPath("$.email").value("updated.admin@okcps.org"))
                        .andExpect(jsonPath("$.firstName").value("Updated"))
                        .andExpect(jsonPath("$.lastName").value("Updated"))
                        .andExpect(jsonPath("$.role").value("ADMIN"));
            }
        }

        @Test
        @DisplayName("updates STAFF user with valid data")
        void updatedStaffUser_withValidData() throws Exception {
            Optional<User> staffUser = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (staffUser.isPresent()) {
                Long staffId = staffUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated.staff@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated",
                        "role": "STAFF"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", staffId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(staffId))
                        .andExpect(jsonPath("$.email").value("updated.staff@okcps.org"))
                        .andExpect(jsonPath("$.firstName").value("Updated"))
                        .andExpect(jsonPath("$.lastName").value("Updated"))
                        .andExpect(jsonPath("$.role").value("STAFF"));
            }
        }

        @Test
        @DisplayName("updates PARA user with valid data")
        void updatedParaUser_withValidData() throws Exception {
            Optional<User> paraUser = userDAO.findByRole(Role.PARA, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (paraUser.isPresent()) {
                Long paraId = paraUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated.para@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated",
                        "role": "PARA"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", paraId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(paraId))
                        .andExpect(jsonPath("$.email").value("updated.para@okcps.org"))
                        .andExpect(jsonPath("$.firstName").value("Updated"))
                        .andExpect(jsonPath("$.lastName").value("Updated"))
                        .andExpect(jsonPath("$.role").value("PARA"));
            }
        }

        @Test
        @DisplayName("returns 404 when updating non-existent admin")
        void returns404_whenUpdatingNonExistentAdmin() throws Exception {
            Long nonExistentId = 9999L;
            String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated",
                    "role": "ADMIN"
                }
                """;
            mockMvc.perform(put(baseUrl + "/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("User not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 when updating STUDENT user")
        void returns404_whenUpdatingStudentUser() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated",
                    "role": "ADMIN"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("STUDENT"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 when updating TEACHER user")
        void returns404_whenUpdatingTeacherUser() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long teacherId = teacherUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated",
                    "role": "ADMIN"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("TEACHER"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 when updating an ADMIN user to TEACHER role")
        void returns400_whenUpdatingAdminToTeacherRole() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long teacherId = adminUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated",
                    "role": "TEACHER"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("TEACHER"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 when updating an ADMIN user to STUDENT role")
        void returns400_whenUpdatingAdminToStudentRole() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long teacherId = adminUser.get().getId();
                String updateJson = """
                {
                    "email": "updated.email@okcps.org",
                    "firstName": "Updated",
                    "lastName": "Updated",
                    "role": "STUDENT"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("STUDENT"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 403 when user tries to update user with disallowed roles")
        void returns403_whenUserTriesToUpdateWithDisallowedRoles() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long adminId = adminUser.get().getId();
                String updateJson = """
                    {
                        "email": "updated@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Updated",
                        "role": "ADMIN"
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", adminId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(DISALLOWED_ROLES))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id} - Delete user")
    class DeleteUser {
        @Test
        @DisplayName("deletes ADMIN user and returns 204")
        void deletesAdminUser_andReturns204() throws Exception {
            Optional<User> existingUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingUser.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", existingUser.get().getId())
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @DisplayName("deletes STAFF user and returns 204")
        void deletesStaffUser_andReturns204() throws Exception {
            Optional<User> existingUser = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingUser.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", existingUser.get().getId())
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @DisplayName("deletes PARA user and returns 204")
        void deletesParaUser_andReturns204() throws Exception {
            Optional<User> existingUser = userDAO.findByRole(Role.PARA, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingUser.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", existingUser.get().getId())
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @DisplayName("returns 404 when deleting non-existent user")
        void returns404_whenDeletingNonExistentUser() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(delete(baseUrl + "/{id}", nonExistentId)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("User not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 when deleting STUDENT user")
        void returns400_whenDeletingStudentUser() throws Exception {
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentUser.isPresent()) {
                Long studentId = studentUser.get().getId();
                mockMvc.perform(delete(baseUrl + "/{id}", studentId)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("STUDENT"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 when deleting TEACHER user")
        void returns404_whenDeletingTeacherUser() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                Long studentId = teacherUser.get().getId();
                mockMvc.perform(delete(baseUrl + "/{id}", studentId)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message", allOf(
                                containsString("User service can only handle users with roles:"),
                                containsString("ADMIN"),
                                containsString("STAFF"),
                                containsString("PARA"),
                                containsString(", but found role: "),
                                containsString("TEACHER"))))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 403 when user tries to delete with disallowed roles")
        void returns403_whenUserTriesToDeleteWithDisallowedRoles() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }
}
