package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.StudentController;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.Student;
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
 * Full-stack integration tests for {@link StudentController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete student management flow from HTTP endpoint through service layer to
 * database, validating system behavior against a production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive student data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 1.5
 * @author Dylan Mercer
 */
@DisplayName("Student Integration Tests")
public class StudentTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDAO studentDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/students";
    }

    @Nested
    @DisplayName("GET /students - Retrieve students")
    class GetAllStudents {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns paginated students with default parameters")
        void returnsPaginatedStudentsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedStudents() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "user.lastName,asc;points,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns empty page when no students exist")
        void returnsEmptyPageWhenNoStudents() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /students/search - Search students")
    class SearchStudents {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("no criteria returns all students")
        void searchNoCriteria_ReturnsAllStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by email returns matching students")
        void searchByEmail_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "student"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.email",
                            everyItem(containsStringIgnoringCase("student"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by first name returns matching students")
        void searchByFirstName_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("firstName", "student"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.firstName",
                            everyItem(containsStringIgnoringCase("student"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by last name returns matching students")
        void searchByLastName_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("lastName", "student"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].user.lastName",
                            everyItem(containsStringIgnoringCase("student"))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by teacher ID returns matching students")
        void searchByTeacherId_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("teacherId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].teacher.id",
                            everyItem(is(1))));

        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by points range returns matching students")
        void searchByPointsRange_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPoints", "0")
                            .param("maxPoints", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].points",
                            everyItem(allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(1000)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by min points returns matching students")
        void searchByMinPoints_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPoints", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].points",
                            everyItem(greaterThanOrEqualTo(0))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by max points returns matching students")
        void searchByMaxPoints_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxPoints", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].points",
                            everyItem(lessThanOrEqualTo(1000))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with combined criteria returns matching students")
        void searchWithCombinedCriteria_ReturnsMatchingStudents() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("teacherId", "1")
                            .param("minPoints", "0")
                            .param("maxPoints", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].teacher.id", everyItem(is(1))))
                    .andExpect(jsonPath("$.content[*].points",
                            everyItem(allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(1000)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_ReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("email", "nonexistentemail@okcps.org"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResultsWhenSortParameterProvided() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("sort", "points,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /students/leaderboard - Classroom leaderboard")
    class ClassroomLeaderboard {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns leaderboard for teacher with default parameters")
        void returnsLeaderboardWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl + "/leaderboard")
                            .param("teacherId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when teacher does not exist")
        void returns404WhenTeacherDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/leaderboard")
                            .param("teacherId", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Teacher not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted classroom leaderboard results when sort parameter provided")
        void returnsSortedClassroomLeaderboardResultsWhenSortParameterProvided() throws Exception {
            mockMvc.perform(get(baseUrl + "/leaderboard")
                            .param("teacherId", "1")
                            .param("sort", "user.lastName,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }
    }

    @Nested
    @DisplayName("GET /students/{id} - Get student by ID")
    class GetStudentsById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns student when ID exists")
        void returnsStudent_whenIdExists() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.user.role").value("STUDENT"))
                    .andExpect(jsonPath("$.points").exists())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.teacher").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIDDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("GET /students/token/{token} - Get student by token")
    class GetStudentsByToken {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns student when token exists")
        void returnsStudent_whenTokenExists() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                String token = student.get().getToken();
                mockMvc.perform(get(baseUrl + "/token/{token}", token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.user.role").value("STUDENT"))
                        .andExpect(jsonPath("$.token").value(token));
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when token does not exist")
        void returns404_whenTokenDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/token/{token}", "non-existent-token"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student not found with token: non-existent-token"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /students - Create student")
    class CreateStudent {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("creates student with valid data")
        void createdStudent_withValidData() throws Exception {
            Long teacherId = 1L;
            String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
            String studentJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": %d
                        }
                    }
                    """.formatted(uniqueEmail, teacherId);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                    .andExpect(jsonPath("$.user.firstName").value("New"))
                    .andExpect(jsonPath("$.user.lastName").value("Student"))
                    .andExpect(jsonPath("$.user.role").value("STUDENT"))
                    .andExpect(jsonPath("$.user.studentId").value(notNullValue()))
                    .andExpect(jsonPath("$.user.teacherId").doesNotExist())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.points").value(0))
                    .andExpect(jsonPath("$.teacher.id").value(teacherId));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with invalid email format")
        void created400_withInvalidEmailFormat() throws Exception {
            String studentJson = """
                    {
                        "user": {
                            "email": "invalid-email",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with invalid email domain")
        void created400_withInvalidEmailDomain() throws Exception {
            String studentJson = """
                    {
                        "user": {
                            "email": "test@example.com",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 409 with duplicate email")
        void created409_withDuplicateEmail() throws Exception {
            Optional<Student> existingStudent = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingStudent.isPresent()) {
                String studentJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """.formatted(existingStudent.get().getUser().getEmail());
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(studentJson)
                                .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("A student with this email already exists")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when teacher does not exist")
        void created404_whenTeacherDoesNotExist() throws Exception {
            String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
            String studentJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 9999
                        }
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Teacher not found with ID: 9999")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank first name")
        void created400_withBlankFirstName() throws Exception {
            String studentJson = """
                    {
                        "user": {
                            "email": "test@example.com",
                            "firstName": "",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank last name")
        void created400_withBlankLastName() throws Exception {
            String studentJson = """
                    {
                        "user": {
                            "email": "test@example.com",
                            "firstName": "New",
                            "lastName": "",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "STAFF"})
        @DisplayName("returns 403 when user is not ADMIN")
        void created403_whenUserIsNotAdmin() throws Exception {
            String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
            String studentJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "New",
                            "lastName": "Student",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """.formatted(uniqueEmail);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /students/{id} - Update student")
    class UpdateStudent {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates student with valid data")
        void updatesStudent_withValidData() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Long studentId = student.get().getId();
                String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
                String updateJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "Updated",
                            "lastName": "Student-Name",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 2
                        }
                    }
                    """.formatted(uniqueEmail);
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentId))
                        .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                        .andExpect(jsonPath("$.user.firstName").value("Updated"))
                        .andExpect(jsonPath("$.user.lastName").value("Student-Name"))
                        .andExpect(jsonPath("$.user.role").value("STUDENT"))
                        .andExpect(jsonPath("$.teacher.id").value(2L));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates student when email is unchanged")
        void updatesStudent_whenEmailUnchanged() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Long studentId = student.get().getId();
                String studentEmail = student.get().getUser().getEmail();
                String updateJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "Updated",
                            "lastName": "Student-Name",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """.formatted(studentEmail);
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.user.email").value(studentEmail));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 409 when updating email to existing student's email")
        void created409_whenUpdatingToExistingEmail() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> existingStudent = studentDAO.findAll(PageRequest.of(2, 1))
                    .stream().findFirst();
            if (student.isPresent() && existingStudent.isPresent()) {
                Long studentId = student.get().getId();
                String updateJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "Updated",
                            "lastName": "Student-Name",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 1
                        }
                    }
                    """.formatted(existingStudent.get().getUser().getEmail());
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A student with this email already exists"));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating to non-existent teacher")
        void returns404_whenUpdatingNonExistentTeacher() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                String uniqueEmail = "unique-student-" + System.currentTimeMillis() + "@okcps.org";
                String updateJson = """
                    {
                        "user": {
                            "email": "%s",
                            "firstName": "Updated",
                            "lastName": "Student-Name",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 9999
                        }
                    }
                    """.formatted(uniqueEmail);
                mockMvc.perform(put(baseUrl + "/{id}", student.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Teacher not found with ID: 9999"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating non-existent student")
        void returns404_whenUpdatingNonExistentStudent() throws Exception {
            String updateJson = """
                {
                    "user": {
                        "email": "updated.email@okcps.org",
                        "firstName": "Updated",
                        "lastName": "Student-Name",
                        "role": "STUDENT"
                    },
                    "teacher": {
                        "id": 2
                    }
                }
                """;
            mockMvc.perform(put(baseUrl + "/{id}", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when non-admin tries to update student")
        void returns403_whenNonAdminTriesToUpdate() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Long studentId = student.get().getId();
                String updateJson = """
                    {
                        "user": {
                            "email": "updated.email@okcps.org",
                            "firstName": "Updated",
                            "lastName": "Student-Name",
                            "role": "STUDENT"
                        },
                        "teacher": {
                            "id": 2
                        }
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", studentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /students/{id} - Delete student")
    class DeleteStudent {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deletes student and returns 204")
        void deletesStudent_andReturns204() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-existent student")
        void returns404_whenDeletingNonExistentStudent() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when non-admin tries to delete student")
        void returns403_whenNonAdminTriesToDelete() throws Exception {
                mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
        }
    }
}
