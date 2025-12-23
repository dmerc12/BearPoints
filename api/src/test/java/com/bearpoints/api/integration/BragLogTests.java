package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.BragLogController;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link BragLogController} utilizing Testcontainers with PostgreSQL and
 * comprehensive test data initialization.
 *
 * <p>Tests the complete brag log management flow from HTTP endpoint through service layer to
 * database, validating system behavior against production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive brag log data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see MockMvc
 * @author Dylan Mercer
 */
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Brag Log Integration Tests")
public class BragLogTests {
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

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    @Autowired
    private BragLogDAO bragLogDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/brag-logs";
    }

    @Nested
    @DisplayName("GET /brag-logs - Retrieve brag logs")
    class GetAllBragLogs {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns paginated brag logs with default parameters")
        void returnsPaginatedBragLogsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Nested
        @DisplayName("GET /brag-logs - Sort direction edge cases")
        class SortDirectionTests {
            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort asc parameters provided")
            void returnsSortedBragLogsAsc() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "student.user.lastName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].studentName").exists());
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort desc parameters provided")
            void returnsSortedBragLogsDesc() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "teacher.user.lastName,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].teacherName").exists());
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when no sort parameter is provided")
            void returnsSortedBragLogsNoDirection() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "studentId"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].studentId").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns empty page when no brag logs exist")
        void returnsEmptyPageWhenNoBragLogsExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /brag-logs/search - Search brag logs")
    class SearchBragLogs {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("no criteria returns all brag logs")
        void noCriteriaReturnsAllBragLogs() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by student name returns matching brag logs")
        void searchByStudentName_ReturnsMatchingBragLogs() throws Exception {
            String searchTerm = "S";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].studentName",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by teacher name returns matching brag logs")
        void searchByTeacherName_ReturnsMatchingBragLogs() throws Exception {
            String searchTerm = "T";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("teacherName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].teacherName",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by grade returns matching brag logs")
        void searchByGrade_ReturnsMatchingBragLogs() throws Exception {
            String grade = GradeLevel.FIRST.name();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("grade", grade))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].grade",
                            everyItem(containsStringIgnoringCase(grade))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by points generated range returns matching brag logs")
        void searchByPointsGeneratedRange_ReturnsMatchingBragLogs() throws Exception {
            Integer minPoints = 3;
            Integer maxPoints = 10;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPoints", String.valueOf(minPoints))
                            .param("maxPoints", String.valueOf(maxPoints)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointsGenerated",
                            everyItem(allOf(greaterThanOrEqualTo(minPoints), lessThanOrEqualTo(maxPoints)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by timestamp date range returns matching brag logs")
        void searchByTimestampDateRange_ReturnsMatchingBragLogs() throws Exception {
            String startDate = LocalDateTime.now().minusDays(3).toString();
            String endDate = LocalDateTime.now().toString();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("startDate", startDate)
                            .param("endDate", endDate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].timestamp",
                            everyItem(allOf(greaterThanOrEqualTo(startDate), lessThanOrEqualTo(endDate)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by student ID returns matching brag logs")
        void searchByStudentId_ReturnsMatchingBragLogs() throws Exception {
            Long studentId = 1L;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentId", String.valueOf(studentId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].studentId",
                            everyItem(equalTo(studentId.intValue()))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by teacher ID returns matching brag logs")
        void searchByTeacherId_ReturnsMatchingBragLogs() throws Exception {
            Long teacherId = 1L;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("teacherId", String.valueOf(teacherId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].teacherId",
                            everyItem(equalTo(teacherId.intValue()))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by notes returns matching brag logs")
        void searchByNotes_ReturnsMatchingBragLogs() throws Exception {
            String searchTerm = "Test brag log";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("notes", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].notes",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_ReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Nested
        @DisplayName("GET /brag-logs/search - Sort direction edge cases")
        class SortDirectionTests {
            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort asc parameters provided")
            void returnsSortedBragLogsAsc() throws Exception {
                String searchTerm = "S";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("studentName", searchTerm)
                                .param("sort", "student.user.firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].studentName",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort desc parameters provided")
            void returnsSortedBragLogsDesc() throws Exception {
                String searchTerm = "S";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("studentName", searchTerm)
                                .param("sort", "pointsGenerated,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].studentName",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when no sort parameter is provided")
            void returnsSortedBragLogsNoDirection() throws Exception {
                String searchTerm = "S";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("studentName", searchTerm)
                                .param("sort", "teacher.user.lastName"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].studentName",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }
        }
    }

    @Nested
    @DisplayName("GET /brag-logs/{id} - Get brag log by ID")
    class GetBragLogById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns brag log when ID exists")
        void returnsBragLog_whenIdExists() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                            .stream().findFirst();
            if (bragLog.isPresent()) {
                mockMvc.perform(get(baseUrl + "/{id}", bragLog.get().getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(bragLog.get().getId()))
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.teacherName").exists())
                        .andExpect(jsonPath("$.studentId").exists())
                        .andExpect(jsonPath("$.teacherId").exists())
                        .andExpect(jsonPath("$.grade").exists())
                        .andExpect(jsonPath("$.behaviorIds").exists())
                        .andExpect(jsonPath("$.behaviors").exists())
                        .andExpect(jsonPath("$.pointsGenerated").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.notes").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Brag log not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /brag-logs - Create brag log")
    class CreateBragLog {
        @Test
        @WithMockUser
        @DisplayName("creates brag log with valid data")
        void createBragLog_withValidData() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String createJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.studentId").value(student.get().getId()))
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.behaviorIds[0]").value(behaviorType.get().getId()))
                        .andExpect(jsonPath("$.behaviors").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsGenerated").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.teacherName").exists())
                        .andExpect(jsonPath("$.teacherId").value(student.get().getTeacher().getId()))
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()));
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 with invalid student")
        void returns400_withInvalidStudent() throws Exception {
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String createJson = """
                        {
                            "studentId": 9999,
                            "behaviorIds": %s
                        }
                        """.formatted(behaviorIds);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Student not found with ID: 9999")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 with missing student")
        void returns400_withMissingStudent() throws Exception {
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String createJson = """
                        {
                            "studentId": "",
                            "behaviorIds": %s
                        }
                        """.formatted(behaviorIds);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.studentId")
                                .value(containsString("Student ID is required")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 with missing behavior IDs")
        void returns400_withMissingBehaviorIds() throws Exception {
                Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (student.isPresent()) {
                    Set<Long> behaviorIds = Set.of();
                    String createJson = """
                            {
                                "studentId": %s,
                                "behaviorIds": %s
                            }
                            """.formatted(student.get().getId(), behaviorIds);
                    mockMvc.perform(post(baseUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(createJson)
                                    .with(csrf()))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.message")
                                    .value(containsString("Validation failed")))
                            .andExpect(jsonPath("$.fieldErrors.behaviorIds")
                                    .value(containsString("At least one behavior is required")))
                            .andExpect(jsonPath("$.timestamp").exists());
                }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 with invalid behavior IDs")
        void returns404_withInvalidBehaviorIds() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Set<Long> behaviorIds = Set.of(9999L);
                String createJson = """
                            {
                                "studentId": %s,
                                "behaviorIds": %s
                            }
                            """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value(
                                containsString("Behavior type not found with ID: 9999")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }
    }

    @Nested
    @DisplayName("PUT /brag-logs/{id} - Update brag log")
    class UpdateBragLog {
        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("updates brag log with valid data")
        void updatesBragLog_withValidData() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String updateJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(bragLog.get().getId()))
                        .andExpect(jsonPath("$.studentId").value(student.get().getId()))
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.behaviorIds[0]").value(behaviorType.get().getId()))
                        .andExpect(jsonPath("$.behaviors").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsGenerated").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.teacherName").exists())
                        .andExpect(jsonPath("$.teacherId").value(student.get().getTeacher().getId()))
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 404 when updating non existent brag log")
        void returns404_whenUpdatingNonExistentBragLog() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String updateJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", 9999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Brag log not found with ID: 9999"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 400 with missing student")
        void returns400_withMissingStudent() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String updateJson = """
                        {
                            "studentId": "",
                            "behaviorIds": %s
                        }
                        """.formatted(behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Validation failed"))
                        .andExpect(jsonPath("$.fieldErrors.studentId").value("Student ID is required"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 404 with invalid student")
        void returns404_withInvalidStudent() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String updateJson = """
                        {
                            "studentId": 9999,
                            "behaviorIds": %s
                        }
                        """.formatted(behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Student not found with ID: 9999"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 400 with missing behavior IDs")
        void returns400_withMissingBehaviorIds() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent()) {
                Set<Long> behaviorIds = Set.of();
                String updateJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Validation failed"))
                        .andExpect(jsonPath("$.fieldErrors.behaviorIds")
                                .value("At least one behavior is required"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 400 with invalid behavior IDs")
        void returns400_withInvalidBehaviorIds() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent()) {
                Set<Long> behaviorIds = Set.of(9999L);
                String updateJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Behavior type not found with ID: 9999"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("returns 403 when user is not TEACHER or ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String updateJson = """
                        {
                            "studentId": %s,
                            "behaviorIds": %s
                        }
                        """.formatted(student.get().getId(), behaviorIds);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /brag-logs/{id} - Delete brag log")
    class DeleteBragLog {
        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("deletes brag log and returns 204")
        void deletesBragLog_andReturns204() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                                .with(csrf()))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER"})
        @DisplayName("returns 404 when deleting non existent brag log")
        void returns404_whenDeletingNonExistentBragLog() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Brag log not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("returns 403 when user is not TEACHER or ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", bragLog.get().getId())
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }
}
