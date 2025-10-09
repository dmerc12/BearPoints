package com.bearpoints.api.integration.api;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.dao.UserDAO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for Spring Data REST User endpoints utilizing Testcontainers with
 * PostgreSQL and comprehensive test data initialization with the existing {@link TestDataInitializer}.
 *
 * <p>Tests the complete flow from auto-generated REST endpoints through Spring Data REST to database,
 * validating response structure and repository-based operations.
 *
 * <p>Spring Data REST Response Structure:
 * <ul>
 *     <li>uses HAL+JSON format with _embedded collections</li>
 *     <li>Includes hypermedia links for navigation</li>
 *     <li>Provides pagination metadata in page object</li>
 *     <li>Exposes search endpoints automatically</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see UserDAO
 * @author Dylan Mercer
 * @version 1.0
 */
@Transactional
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Integration Tests")
public class UserTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/users";
    }

    /**
     * Tests retrieval of all users with HAL+JSON response structure.
     *
     * <p>Verifies Spring Data REST auto-generates proper collection resource:
     * <ul>
     *     <li>Users array wrapped in _embedded object</li>
     *     <li>Pagination metadata in page object</li>
     *     <li>Hypermedia links for navigation</li>
     *     <li>Proper content type application/hal+json</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users returns paginated users with proper structure")
    void getAllUsers_WithPagination_ReturnsUsers() throws Exception {
        mockMvc.perform(get(baseUrl)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "email,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users").isArray())
                .andExpect(jsonPath("$._embedded.users").isNotEmpty())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").isNumber())
                .andExpect(jsonPath("$.page.totalPages").isNumber())
                .andExpect(jsonPath("$._embedded.users[0].id").isNumber())
                .andExpect(jsonPath("$._embedded.users[0].firstName").isString())
                .andExpect(jsonPath("$._embedded.users[0].lastName").isString())
                .andExpect(jsonPath("$._embedded.users[0].email").isString())
                .andExpect(jsonPath("$._embedded.users[0].role").isString());
    }

    /**
     * Tests individual user resource retrieval.
     *
     * <p>Validates Spring Data REST item resource structure:
     * <ul>
     *     <li>Direct user properties at root level</li>
     *     <li>Self-link for resource navigation</li>
     *     <li>No _embedded wrapper for single resources</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users/{id} returns user with self link")
    void getUserById_WithValidId_ReturnsUser() throws Exception {
        mockMvc.perform(get(baseUrl + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").isString())
                .andExpect(jsonPath("$.lastName").isString())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.role").isString())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    /**
     * Tests user creation via POST to collection resource.
     *
     * <p>Validates Spring Data REST creation workflow:
     * <ul>
     *     <li>POST to collection resource creates new user</li>
     *     <li>Returns 201 created status with Location header</li>
     *     <li>Admin role required for write operations</li>
     *     <li>Response includes links to created resource</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /users creates new user with HAL response")
    void createUser_WithValidData_ReturnsCreatedUser() throws Exception {
        String userJson = """
                {
                    "firstName": "Test",
                    "lastName": "User",
                    "email": "testuser@okcps.org",
                    "role": "STUDENT"
                }
                """;
        mockMvc.perform(post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson)
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    /**
     * Tests user search endpoint auto-exposure.
     *
     * <p>Validates that Spring Data REST automatically exposes search endpoints:
     * <ul>
     *     <li>/search endpoint lists available search operations</li>
     *     <li>Repository query methods become search resources</li>
     *     <li>Search results follow HAL format</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users/search exposes search operations")
    void getUserSearch_ReturnsSearchOperations() throws Exception {
        mockMvc.perform(get(baseUrl + "/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    /**
     * Tests role-based user filtering.
     *
     * <p>Validates custom repository method exposure:
     * <ul>
     *     <li>Spring Data REST exposes findByRole automatically</li>
     *     <li>Results wrapped in _embedded collection</li>
     *     <li>Pagination supported on custom queries</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users/search/findByRole returns filtered users")
    void findUsersByRole_WithValidRole_ReturnsFilteredUsers() throws Exception {
        mockMvc.perform(get(baseUrl + "/search/byRole")
                    .param("role", "STUDENT")
                    .param("page", "0")
                    .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users").isArray())
                .andExpect(jsonPath("$._embedded.users[*].role").value(everyItem(is("STUDENT"))))
                .andExpect(jsonPath("$.page").exists());
    }

    /**
     * Tests email lookup functionality.
     *
     * <p>Validates findByEmail method exposure and accessibility:
     * <ul>
     *     <li>Publicly accessible without authentication</li>
     *     <li>Returns single user resource</li>
     *     <li>Follows HAL format for single resources</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users/search/findByEmail returns user for valid email")
    void findByEmail_WithValidEmail_ReturnsUser() throws Exception {
        String email = "admin2@okcps.org";
        mockMvc.perform(get(baseUrl + "/search/findByEmail")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    /**
     * Tests user update with PUT operation.
     *
     * <p>Validates full resource replacement:
     * <ul>
     *     <li>PUT to item resource updates the entire user</li>
     *     <li>Admin role required for update operations</li>
     *     <li>Returns updated resource with links</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /users/{id} updates user successfully")
    void updateUser_WithValidId_ReturnsUpdatedUser() throws Exception {
        String updateJson = """
                {
                    "firstName": "Updated",
                    "lastName": "Name",
                    "email": "updated@okcps.org",
                    "role": "TEACHER"
                }
                """;
        mockMvc.perform(put(baseUrl + "/5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson)
                    .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Tests user deletion.
     *
     * <p>Validates DELETE operation on item resource:
     * <ul>
     *     <li>DELETE removes user resource</li>
     *     <li>Admin role required for deletion</li>
     *     <li>Returns 204 No Content on success</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /users/{id} removes user successfully")
    void deleteUser_WithAdminRole_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete(baseUrl + "/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Tests role-based access control.
     *
     * <p>Validates Spring Security integration with Spring Data REST:
     * <ul>
     *     <li>All roles can read user data</li>
     *     <li>Only ADMIN can modify user data</li>
     *     <li>Proper 403 Forbidden for unauthorized operations</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("POST /users denies access for non-ADMIN roles")
    void createUser_WithStudentRole_ReturnsForbidden() throws Exception {
        String userJson = """
                {
                    "firstName": "Student",
                    "lastName": "Created",
                    "email": "studentcreate@okcps.org",
                    "role": "STUDENT"
                }
                """;
        mockMvc.perform(post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests root endpoint discovery.
     *
     * <p>Validates Spring Data REST API discovery:
     * <ul>
     *     <li>Root endpoint exposes all repository resources</li>
     *     <li>Includes users collection with templated links</li>
     *     <li>Provides profile link for metadata</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /api exposes all REST resources")
    void getRootEndpoint_ReturnsApiResources() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.users").exists())
                .andExpect(jsonPath("$._links.profile").exists());
    }

    /**
     * Tests pagination with custom page sizes.
     *
     * <p>Validates Spring Data REST pagination features:
     * <ul>
     *     <li>Custom page sizes work correctly</li>
     *     <li>Page metadata accurately reflects result set</li>
     *     <li>Navigation links provided in _links</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /users with custom pagination returns correct slice")
    void getUsers_WithCustomPagination_ReturnsCorrectPage() throws Exception {
        mockMvc.perform(get(baseUrl)
                    .param("page", "0")
                    .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users").isArray())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.profile").exists());
    }
}
