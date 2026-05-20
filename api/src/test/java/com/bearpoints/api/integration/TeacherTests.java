package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.TeacherController;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for {@link TeacherController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete teacher management flow from HTTP endpoint through service layer to
 * database, validating system behavior against a production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive teacher data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 2.0
 * @author Dylan Mercer
 */
@DisplayName("Teacher Integration Tests")
public class TeacherTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired private UserDAO userDAO;

    private static String baseUrl;

    private static final RequestPostProcessor WRITE_ROLES = user("admin").roles("ADMIN", "STAFF");
    private static final RequestPostProcessor DISALLOWED_ROLES = user("disallowed").roles("STUDENT", "TEACHER", "PARA");
    private static final RequestPostProcessor READ_ROLES = user("any").roles("STUDENT", "TEACHER", "ADMIN", "STAFF", "PARA");

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/teachers";
    }

    @Nested
    @DisplayName("GET /teachers - Retrieve teachers")
    class GetAllTeachers {
        @Test
        @DisplayName("returns paginated teachers with default parameters")
        void returnsPaginatedTeachersWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedTeachersAsc() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "user.lastName,asc;grade,desc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("returns empty page when no teachers exist")
        void returnsEmptyPageWhenNoTeachers() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /teachers/search - Search teachers")
    class SearchTeachers {
        @Test
        @DisplayName("with email criteria returns matching teachers")
        void searchWithEmailCriteria_ReturnsMatchingTeachers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "teacher")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.email",
                            everyItem(containsStringIgnoringCase("teacher"))));
        }

        @Test
        @DisplayName("with first name criteria returns matching teachers")
        void searchWithFirstNameCriteria_ReturnsMatchingTeachers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("firstName", "teacher")
                            .param("sort", "user.firstName")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.firstName",
                            everyItem(containsStringIgnoringCase("teacher"))));
        }

        @Test
        @DisplayName("with last name criteria returns matching teachers")
        void searchWithLastNameCriteria_ReturnsMatchingTeachers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "teacher")
                            .param("sort", "user.lastName,asc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.lastName",
                            everyItem(containsStringIgnoringCase("teacher"))));
        }

        @Test
        @DisplayName("by grade level returns matching teachers")
        void searchByGrade_ReturnsMatchingTeachers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("grade", "FIRST")
                            .param("sort", "user.firstName,desc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].grade",
                            everyItem(is("FIRST"))));
        }

        @Test
        @DisplayName("by grade level handles case-insensitive grade strings")
        void searchByGrade_HandlesCaseInsensitiveGrade() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("grade", "first")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].grade",
                            everyItem(is("FIRST"))));
        }

        @Test
        @DisplayName("by grade level handles hyphenated grade strings")
        void searchByGrade_HandlesHyphenatedGrade() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("grade", "pre-k")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].grade",
                            everyItem(is("PRE_K"))));
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
        @DisplayName("with non-matching grade returns empty results")
        void searchWithNonMatchingGrade_returnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("grade", "NONEXISTENT_GRADE")
                            .with(READ_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Invalid grade level")));
        }

        @Test
        @DisplayName("with no criteria returns all teachers")
        void searchWithNoCriteria_ReturnsAllTeachers() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "")
                            .param("sort", "user.firstName,asc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /teachers/{id} - Get teacher by ID")
    class GetTeacherById {
        @Test
        @DisplayName("returns teacher when ID exists")
        void returnsTeacher_whenIdExists() throws Exception {
            Optional<Teacher> teacher = teacherDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacher.isPresent()) {
                Long teacherId = teacher.get().getId();
                mockMvc.perform(get(baseUrl + "/{id}", teacherId)
                                .with(READ_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(teacherId))
                        .andExpect(jsonPath("$.user.role").value("TEACHER"))
                        .andExpect(jsonPath("$.grade").exists());
            }
        }

        @Test
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExists() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(get(baseUrl + "/{id}", nonExistentId)
                            .with(READ_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Teacher not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /teachers - Create teacher")
    class CreateTeacher {
        @Test
        @DisplayName("creates teacher with valid data")
        void createdTeacher_withValidData() throws Exception {
            String uniqueEmail = "unique-teacher-" + System.currentTimeMillis() + "@okcps.org";
            String teacherJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.user.firstName").value("New"))
                    .andExpect(jsonPath("$.user.lastName").value("Teacher"))
                    .andExpect(jsonPath("$.user.role").value("TEACHER"))
                    .andExpect(jsonPath("$.user.teacherId").value(notNullValue()))
                    .andExpect(jsonPath("$.user.studentId").doesNotExist())
                    .andExpect(jsonPath("$.grade").value("FIRST"));
        }

        @Test
        @DisplayName("returns 400 with invalid email format")
        void returns400_withInvalidEmailFormat() throws Exception {
            String teacherJson = """
                    {
                        "user": {
                            "email": "invalid-email",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 400 with invalid email domain")
        void returns400_withInvalidEmailDomain() throws Exception {
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@example.com",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 400 with duplicate email")
        void returns400_withDuplicateEmail() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                String teacherEmail = teacherUser.get().getEmail();
                String teacherJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """.formatted(teacherEmail);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teacherJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("A user with this email already exists")));
            }
        }

        @Test
        @DisplayName("returns 400 with invalid grade level")
        void returns400_withInvalidGradeLevel() throws Exception {
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@okcps.org",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "INVALID_GRADE"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Invalid grade level")));
        }

        @Test
        @DisplayName("returns 400 with blank first name")
        void returns400_withBlankFirstName() throws Exception {
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@okcps.org",
                            "firstName": "",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 400 with first name too long")
        void returns400_withFirstNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@okcps.org",
                            "firstName": "%s",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 400 with blank last name")
        void returns400_withBlankLastName() throws Exception {
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@okcps.org",
                            "firstName": "New",
                            "lastName": "",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 400 with last name too long")
        void returns400_withLastNameTooLong() throws Exception {
            String longName = "A".repeat(101);
            String teacherJson = """
                    {
                        "user": {
                            "email": "test@okcps.org",
                            "firstName": "New",
                            "lastName": "%s",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """.formatted(longName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")));
        }

        @Test
        @DisplayName("returns 403 when user has disallowed roles")
        void returns403_whenUserDisallowedRole() throws Exception {
            String uniqueEmail = "unique-teacher-" + System.currentTimeMillis() + "@okcps.org";
            String teacherJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Teacher",
                            "role": "TEACHER"
                        },
                        "grade": "FIRST"
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(teacherJson)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /teachers/{id} - Update teacher")
    class UpdateTeacher {
        @Test
        @DisplayName("updates teacher with valid data")
        void updatedTeacher_withValidData() throws Exception {
            Optional<Teacher> teacher = teacherDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacher.isPresent()) {
                Long teacherId = teacher.get().getId();
                String updateJson = """
                        {
                            "user" : {
                                "email": "updated.email@okcps.org",
                                "firstName": "Updated",
                                "lastName": "Teacher",
                                "role": "TEACHER"
                            },
                            "grade": "SECOND"
                        }
                        """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf())
                                .with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(teacherId))
                        .andExpect(jsonPath("$.user.email").value("updated.email@okcps.org"))
                        .andExpect(jsonPath("$.user.firstName").value("Updated"))
                        .andExpect(jsonPath("$.user.lastName").value("Teacher"))
                        .andExpect(jsonPath("$.grade").value("SECOND"));
            }
        }

        @Nested
        @DisplayName("PUT /teachers/{id} - Email update scenarios")
        class UpdateTeacherEmailTests {
            @Test
            @DisplayName("updates teacher when email is unchanged")
            void updatesTeacher_whenEmailUnchanged() throws Exception {
                Optional<Teacher> teacher = teacherDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (teacher.isPresent()) {
                    Long teacherId = teacher.get().getId();
                    String originalEmail = teacher.get().getUser().getEmail();
                    String updateJson = """
                            {
                                "user" : {
                                    "email": "%s",
                                    "firstName": "Updated",
                                    "lastName": "Teacher",
                                    "role": "TEACHER"
                                },
                                "grade": "SECOND"
                            }
                            """.formatted(originalEmail);
                    mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(updateJson)
                                    .with(csrf()).with(WRITE_ROLES))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.user.email").value(originalEmail));
                }
            }

            @Test
            @DisplayName("returns 409 when updating email to existing teacher's email")
            void returns409_whenUpdatingToExistingEmail() throws Exception {
                Page<Teacher> teachers = teacherDAO.findAll(PageRequest.of(0, 2));
                List<Teacher> teacherList = teachers.getContent();
                if (teacherList.size() >= 2) {
                    String updateJson = """
                            {
                                "user" : {
                                    "email": "%s",
                                    "firstName": "Updated",
                                    "lastName": "Teacher",
                                    "role": "TEACHER"
                                },
                                "grade": "SECOND"
                            }
                            """.formatted(teacherList.getLast().getUser().getEmail());
                    mockMvc.perform(put(baseUrl + "/{id}", teacherList.getFirst().getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(updateJson)
                                    .with(csrf()).with(WRITE_ROLES))
                            .andExpect(status().isConflict())
                            .andExpect(jsonPath("$.message")
                                    .value("A user with this email already exists"));
                }
            }

            @Test
            @DisplayName("updates teacher when email is changed to new unique email")
            void updatesTeacher_whenEmailChangedToUnique() throws Exception {
                Optional<Teacher> teacher = teacherDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (teacher.isPresent()) {
                    String newEmail = "unique-updated-" + System.currentTimeMillis() + "@okcps.org";
                    String updateJson = """
                            {
                                "user" : {
                                    "email": "%s",
                                    "firstName": "Updated",
                                    "lastName": "Teacher",
                                    "role": "TEACHER"
                                },
                                "grade": "SECOND"
                            }
                            """.formatted(newEmail);
                    mockMvc.perform(put(baseUrl + "/{id}", teacher.get().getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(updateJson)
                                    .with(csrf()).with(WRITE_ROLES))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.user.email").value(newEmail));
                }
            }
        }

        @Test
        @DisplayName("returns 404 when updating non-existent teacher")
        void returns404_whenUpdatingNonExistentTeacher() throws Exception {
            Long nonExistentId = 9999L;
            String updateJson = """
                        {
                            "user" : {
                                "email": "updated.email@okcps.org",
                                "firstName": "Updated",
                                "lastName": "Teacher",
                                "role": "TEACHER"
                            },
                            "grade": "SECOND"
                        }
                        """;
            mockMvc.perform(put(baseUrl + "/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Teacher not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 403 when user tries to update teacher and has disallowed role")
        void returns403_whenUserTriesToUpdateWithDisallowedRole() throws Exception {
            Optional<Teacher> teacher = teacherDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacher.isPresent()) {
                Long teacherId = teacher.get().getId();
                String updateJson = """
                {
                    "user" : {
                        "email": "updated@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Teacher",
                        "role": "TEACHER"
                    },
                    "grade": "SECOND"
                }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", teacherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(DISALLOWED_ROLES))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /teachers/{id} - Delete teacher")
    class DeleteTeacher {
        @Test
        @DisplayName("deletes teacher and returns 204")
        void deletesTeacher_andReturns204() throws Exception {
            Optional<Teacher> existingTeacher = teacherDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingTeacher.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", existingTeacher.get().getId())
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @DisplayName("returns 404 when deleting non-existent teacher")
        void returns404_whenDeletingNonExistentTeacher() throws Exception {
            Long nonExistentId = 9999L;
            mockMvc.perform(delete(baseUrl + "/{id}", nonExistentId)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Teacher not found with ID: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 403 when user tries to delete teacher and has disallowed role")
        void returns403_whenUserTriesToDeleteWithDisallowedRole() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }
}
