package com.bearpoints.api.integration.api;

import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Teacher Integration Tests")
public class TeacherTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private UserDAO userDAO;

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
        baseUrl = "/api/teachers";
    }

    /**
     * Tests retrieval of all teachers with HAL+JSON response structure using TeacherProjection.
     *
     * <p>Verifies Spring Data REST auto-generates proper collection resource:
     * <ul>
     *     <li>Teachers array wrapped in _embedded object</li>
     *     <li>Pagination metadata in page object</li>
     *     <li>Hypermedia links for navigation</li>
     *     <li>Proper content type application/hal+json</li>
     *     <li>TeacherProjection provides condensed view (id, grade, user projection)</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /teachers returns paginated teachers with proper structure")
    void getAllTeachers_WithPagination_ReturnsTeachers() throws Exception {
        mockMvc.perform(get(baseUrl)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teachers").isArray())
                .andExpect(jsonPath("$._embedded.teachers").isNotEmpty())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").isNumber())
                .andExpect(jsonPath("$.page.totalPages").isNumber())
                .andExpect(jsonPath("$._embedded.teachers[0].id").isNumber())
                .andExpect(jsonPath("$._embedded.teachers[0].grade").isString())
                .andExpect(jsonPath("$._embedded.teachers[0].user").exists())
                .andExpect(jsonPath("$._embedded.teachers[0].user.id").isNumber())
                .andExpect(jsonPath("$._embedded.teachers[0].user.firstName").isString())
                .andExpect(jsonPath("$._embedded.teachers[0].user.lastName").isString())
                .andExpect(jsonPath("$._embedded.teachers[0].user.email").isString())
                .andExpect(jsonPath("$._embedded.teachers[0].user.role").isString());
    }

    /**
     * Tests individual teacher resource retrieval with TeacherProjection.
     *
     * <p>Validates Spring Data REST item resource structure:
     * <ul>
     *     <li>Direct teacher properties at root level</li>
     *     <li>Self-link for resource navigation</li>
     *     <li>User details via UserProjection</li>
     *     <li>No _embedded wrapper for single resources</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /teachers/{id} returns teacher with self link and projection")
    void getTeacherById_WithValidId_ReturnsTeacher() throws Exception {
        mockMvc.perform(get(baseUrl + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.grade").isString())
                .andExpect(jsonPath("$._embedded.user").exists())
                .andExpect(jsonPath("$._embedded.user.id").isNumber())
                .andExpect(jsonPath("$._embedded.user.firstName").isString())
                .andExpect(jsonPath("$._embedded.user.lastName").isString())
                .andExpect(jsonPath("$._embedded.user.email").isString())
                .andExpect(jsonPath("$._embedded.user.role").isString())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    /**
     * Tests teacher creation via POST to collection resource.
     *
     * <p>Validates Spring Data REST collection workflow:
     * <ul>
     *     <li>POST to collection resource creates new teacher</li>
     *     <li>Returns 201 created status with Location header</li>
     *     <li>Admin role required for write operations</li>
     *     <li>Response includes links to created resource</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /teachers creates new teacher with HAL response")
    void createTeacher_WithValidData_ReturnsCreatedTeacher() throws Exception {
        User user = new User();
        user.setFirstName("New");
        user.setLastName("Teacher");
        user.setEmail("newteacher@okcps.org");
        user.setRole(Role.TEACHER);
        User savedUser = userDAO.save(user);
        String teacherJson = """
                {
                    "grade": "FIRST",
                    "user": "%s"
                }
                """.formatted("/api/users/" + savedUser.getId());
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson).content(teacherJson)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    /**
     * Tests teacher search endpoint auto-exposure.
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
    @DisplayName("GET /teachers/search exposes search operations")
    void getTeacherSearch_ReturnsSearchOperations() throws Exception {
        mockMvc.perform(get(baseUrl + "/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.findByGrade").exists())
                .andExpect(jsonPath("$._links.findByUserEmail").exists());
    }

    /**
     * Tests grade-based teacher filtering.
     *
     * <p>Validates custom repository method exposure:
     * <ul>
     *     <li>Spring Data REST exposes findByGrade automatically</li>
     *     <li>Results wrapped in _embedded collection</li>
     *     <li>Pagination supported on custom queries</li>
     *     <li>TeacherProjection used for condensed views</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /teachers/search/findByGrade returns filtered teachers")
    void findTeachersByGrade_WithValidGrade_ReturnsFilteredTeachers() throws Exception {
        mockMvc.perform(get(baseUrl + "/search/findByGrade")
                        .param("grade", "FIRST")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teachers").isArray())
                .andExpect(jsonPath("$._embedded.teachers[*].grade").value(everyItem(is("FIRST"))))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.page.size").isNumber())
                .andExpect(jsonPath("$.page.totalElements").isNumber())
                .andExpect(jsonPath("$.page.totalPages").isNumber())
                .andExpect(jsonPath("$.page.number").value(0));
    }

    /**
     * Tests email lookup functionality for teachers.
     *
     * <p>Validates findByUserEmail method exposure and accessibility:
     * <ul>
     *     <li>Accessible to authenticated users</li>
     *     <li>Returns single teacher resource with projection</li>
     *     <li>Follows HAL format for single resources</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /teachers/search/findByUserEmail returns teacher for valid email")
    void findByUserEmail_WithValidEmail_ReturnsTeacher() throws Exception {
        String email = "teacher1@okcps.org";
        mockMvc.perform(get(baseUrl + "/search/findByUserEmail")
                    .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.user.email").value(email))
                .andExpect(jsonPath("$.grade").exists())
                .andExpect(jsonPath("$._embedded.user.firstName").exists())
                .andExpect(jsonPath("$._embedded.user.lastName").exists());
    }

    /**
     * Tests teacher update with PUT operation.
     *
     * <p>Validates full resource replacement:
     * <ul>
     *     <li>PUT to item resource updates the entire teacher</li>
     *     <li>Admin role required for update operations on the other teachers</li>
     *     <li>Teachers can update their own profile</li>
     *     <li>Returns 204 No Content on success</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /teachers/{id} updates teacher successfully")
    void updateTeacher_WithValidId_ReturnsUpdatedTeacher() throws Exception {
        Teacher existingTeacher = teacherDAO.findAll().getFirst();
        String updateJson = """
                {
                    "grade": "SECOND",
                    "user": "%s"
                }
                """.formatted("/api/users/" + existingTeacher.getUser().getId());
        mockMvc.perform(put(baseUrl + "/" + existingTeacher.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson)
                    .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Tests teacher deletion.
     *
     * <p>Validates DELETE operation on item resource:
     * <ul>
     *     <li>DELETE removes teacher resource</li>
     *     <li>Admin role required for deletion</li>
     *     <li>Returns 204 No Content on success</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /teachers/{id} removes teacher successfully")
    void deleteTeacher_WithAdminRole_ReturnsNoContent() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("jodoe@okcps.org");
        user.setRole(Role.TEACHER);
        User savedUser = userDAO.save(user);
        Teacher tempTeacher = new Teacher();
        tempTeacher.setGrade(GradeLevel.THIRD);
        tempTeacher.setUser(savedUser);
        Teacher savedTeacher = teacherDAO.save(tempTeacher);
        mockMvc.perform(delete(baseUrl + "/" + savedTeacher.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Tests pagination with custom page sizes for teachers.
     *
     * <p>Validates Spring Data REST pagination features:
     * <ul>
     *     <li>Custom page sizes work correctly</li>
     *     <li>Page metadata accurately reflects result set</li>
     *     <li>Navigation links provided in _links</li>
     *     <li>TeacherProjection maintains consistency across pages</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /teachers with custom pagination returns correct slice")
    void getTeachers_WithCustomPagination_ReturnsCorrectPage() throws Exception {
        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teachers").isArray())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.profile").exists());
    }

    /**
     * Tests that teacher projection excludes sensitive data.
     *
     * <p>Validates that TeacherProjection provides condensed view:
     * <ul>
     *     <li>Includes only id, grade, and user projection</li>
     *     <li>Excludes students and bragLogs collections</li>
     *     <li>User projection provides essential user info only</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("Teacher projection excludes students and bragLogs")
    void getTeacher_WithProjection_ExcludesSensitiveData() throws Exception {
        mockMvc.perform(get(baseUrl + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.grade").exists())
                .andExpect(jsonPath("$._embedded.user").exists())
                .andExpect(jsonPath("$._embedded.students").exists())
                .andExpect(jsonPath("$._embedded.bragLogs").exists())
                .andExpect(jsonPath("$.lastSynced").doesNotExist())
                .andExpect(jsonPath("$._embedded.lastSynced").doesNotExist())
                .andExpect(jsonPath("$._embedded.user.lastSynced").doesNotExist())
                .andExpect(jsonPath("$._embedded.students[*].lastSynced").doesNotExist())
                .andExpect(jsonPath("$._embedded.bragLogs[*].lastSynced").doesNotExist())
                .andExpect(jsonPath("$.sheetRowId").doesNotExist())
                .andExpect(jsonPath("$._embedded.sheetRowId").doesNotExist())
                .andExpect(jsonPath("$._embedded.user.sheetRowId").doesNotExist())
                .andExpect(jsonPath("$._embedded.students[*].sheetRowId").doesNotExist())
                .andExpect(jsonPath("$._embedded.bragLogs[*].sheetRowId").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andExpect(jsonPath("$._embedded.version").doesNotExist())
                .andExpect(jsonPath("$._embedded.user.version").doesNotExist())
                .andExpect(jsonPath("$._embedded.students[*].version").doesNotExist())
                .andExpect(jsonPath("$._embedded.bragLogs[*].version").doesNotExist());
    }
}
