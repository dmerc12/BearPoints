package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.BragLogController;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link BragLogController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
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
 * @see BaseIntegrationTest
 * @version 2.2
 * @author Dylan Mercer
 */
@DisplayName("Brag Log Integration Tests")
public class BragLogTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    @Autowired
    private BragLogDAO bragLogDAO;

    @Autowired
    private UserDAO userDAO;

    private static String baseUrl;

    private static final String VALID_SUBMITTER_NAME = "Integration Tester";

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/brags";
    }

    @Nested
    @DisplayName("GET /api/brags - Retrieve brag logs")
    class GetAllBragLogs {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns paginated brag logs with default parameters")
        void returnsPaginatedBragLogsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedBragLogs() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "timestamp,desc;student.points,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns empty page when no brag logs exist")
        void returnsEmptyPageWhenNoBragLogsExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/brags/search - Search brag logs")
    class SearchBragLogs {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("no criteria returns all brag logs")
        void noCriteriaReturnsAllBragLogs() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by points generated min points only returns matching brag logs")
        void searchByMinPointsGenerated_ReturnsMatchingBragLogs() throws Exception {
            Integer minPoints = 3;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPoints", String.valueOf(minPoints)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointsGenerated",
                            everyItem(allOf(greaterThanOrEqualTo(minPoints)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by points generated max points only returns matching brag logs")
        void searchByMaxPointsGenerated_ReturnsMatchingBragLogs() throws Exception {
            Integer maxPoints = 10;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxPoints", String.valueOf(maxPoints)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointsGenerated",
                            everyItem(allOf(lessThanOrEqualTo(maxPoints)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by timestamp start date only returns matching brag logs")
        void searchByTimestampStartDate_ReturnsMatchingBragLogs() throws Exception {
            String startDate = LocalDateTime.now().minusDays(3).toString();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("startDate", startDate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].timestamp",
                            everyItem(allOf(greaterThanOrEqualTo(startDate)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by timestamp end date only returns matching brag logs")
        void searchByTimestampEndDate_ReturnsMatchingBragLogs() throws Exception {
            String endDate = LocalDateTime.now().toString();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("endDate", endDate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].timestamp",
                            everyItem(allOf(lessThanOrEqualTo(endDate)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by submitter name returns matching brag logs")
        void searchBySubmitterName_ReturnsMatchingBragLogs() throws Exception {
            String searchTerm = "Admin";
            mockMvc.perform(get(baseUrl + "/search")
                        .param("submitterName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].submitterName",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by empty submitter name returns all brag logs")
        void searchByEmptySubmitterName_ReturnsAllBragLogs() throws Exception {
            String searchTerm = "";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("submitterName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by submitter user ID returns matching brag logs")
        void searchBySubmitterUserId_ReturnsMatchingBragLogs() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                Long userId = adminUser.get().getId();
                mockMvc.perform(get(baseUrl + "/search")
                                .param("submitterUserId", String.valueOf(userId)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].submitterUserId",
                                everyItem(equalTo(userId.intValue()))));
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_ReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with combined criteria returns matching brag logs")
        void searchWithCombinedCriteria_ReturnsMatchingBragLogs() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                String studentNameSearch = "";
                String teacherNameSearch = "";
                String grade = GradeLevel.FIRST.name();
                Integer minPoints = 3;
                Integer maxPoints = 10;
                String startDate = LocalDateTime.now().minusDays(3).toString();
                String endDate = LocalDateTime.now().toString();
                Long studentId = 1L;
                Long teacherId = 1L;
                String notesSearch = "";
                String submitterNameSearch = adminUser.get().getFirstName() + " " + adminUser.get().getLastName();
                Long submitterUserId = adminUser.get().getId();
                mockMvc.perform(get(baseUrl + "/search")
                                .param("studentName", studentNameSearch)
                                .param("teacherName", teacherNameSearch)
                                .param("grade", grade)
                                .param("minPoints", String.valueOf(minPoints))
                                .param("maxPoints", String.valueOf(maxPoints))
                                .param("startDate", startDate)
                                .param("endDate", endDate)
                                .param("studentId", String.valueOf(studentId))
                                .param("teacherId", String.valueOf(teacherId))
                                .param("notes", notesSearch)
                                .param("submitterName", submitterNameSearch)
                                .param("submitterUserId", String.valueOf(submitterUserId))
                                .param("sort", "grade,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content").isArray());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/brags/{id} - Get brag log by ID")
    class GetBragLogById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
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
                        .andExpect(jsonPath("$.notes").exists())
                        .andExpect(jsonPath("$.submitterName").exists())
                        .andExpect(jsonPath("$.submitterUserId").value(nullValue()));
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Brag log not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/brags - Create brag log")
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
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()))
                        .andExpect(jsonPath("$.submitterName").value(VALID_SUBMITTER_NAME))
                        .andExpect(jsonPath("$.submitterUserId").doesNotExist());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("creates brag log with null notes")
        void createBragLog_withNullNotes() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, null);
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
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()))
                        .andExpect(jsonPath("$.submitterName").value(VALID_SUBMITTER_NAME))
                        .andExpect(jsonPath("$.submitterUserId").doesNotExist());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("creates brag log and links to existing ADMIN user when submitter name matches")
        void createBragLog_withAdminSubmitter_linksUser() throws Exception {
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (adminUser.isPresent()) {
                String adminFullName = adminUser.get().getFirstName() + " " + adminUser.get().getLastName();
                Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (student.isPresent() && behaviorType.isPresent()) {
                    Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                    String createJson = buildBragLogJson(student.get().getId(), behaviorIds, adminFullName, null);
                    mockMvc.perform(post(baseUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(createJson)
                                    .with(csrf()))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.submitterUserId").value(adminUser.get().getId()));
                }
            }
        }

        @Test
        @WithMockUser
        @DisplayName("creates brag log and links to existing STAFF user when submitter name matches")
        void createBragLog_withStaffSubmitter_linksUser() throws Exception {
            Optional<User> staffUser = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (staffUser.isPresent()) {
                String staffFullName = staffUser.get().getFirstName() + " " + staffUser.get().getLastName();
                Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (student.isPresent() && behaviorType.isPresent()) {
                    Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                    String createJson = buildBragLogJson(student.get().getId(), behaviorIds, staffFullName, null);
                    mockMvc.perform(post(baseUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(createJson)
                                    .with(csrf()))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.submitterUserId").value(staffUser.get().getId()));
                }
            }
        }

        @Test
        @WithMockUser
        @DisplayName("creates brag log and links to existing TEACHER user when submitter name matches")
        void createBragLog_withTeacherSubmitter_linksUser() throws Exception {
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (teacherUser.isPresent()) {
                String teacherFullName = teacherUser.get().getFirstName() + " " + teacherUser.get().getLastName();
                Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                        .stream().findFirst();
                if (student.isPresent() && behaviorType.isPresent()) {
                    Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                    String createJson = buildBragLogJson(student.get().getId(), behaviorIds, teacherFullName, null);
                    mockMvc.perform(post(baseUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(createJson)
                                    .with(csrf()))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.submitterUserId").value(teacherUser.get().getId()));
                }
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when submitter name is null")
        void returns400_whenSubmitterNameIsNull() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, null, notes);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.submitterName")
                                .value(containsString("Submitter name is required")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when submitter name is blank")
        void returns400_whenSubmitterNameIsBlank() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, "", notes);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.submitterName")
                                .value(containsString("Submitter name must be between 2 and 250 characters")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when submitter name has no space")
        void returns400_whenSubmitterNameHasNoSpace() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, "SingleWord", notes);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value(
                                containsString("Submitter name must contain both first and last name")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when submitter name matches STUDENT user")
        void returns400_whenSubmitterNameMatchesStudentUser() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                User studentUser = student.get().getUser();
                String studentFullName = studentUser.getFirstName() + " " + studentUser.getLastName();
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, studentFullName, notes);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value(
                                containsString("Students cannot submit brag logs")))
                        .andExpect(jsonPath("$.timestamp").exists());
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
                String notes = "Notes";
                String createJson = buildBragLogJson(9999L, behaviorIds, VALID_SUBMITTER_NAME, notes);
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
                String notes = "Notes";
                String createJson = buildBragLogJson(null, behaviorIds, VALID_SUBMITTER_NAME, notes);
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
                    String notes = "Notes";
                    String createJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
                String notes = "Notes";
                String createJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
    @DisplayName("PUT /api/brags/{id} - Update brag log")
    class UpdateBragLog {
        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
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
                String newSubmitterName = "Updated Tester";
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, newSubmitterName, notes);
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
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()))
                        .andExpect(jsonPath("$.submitterName").value(newSubmitterName));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates student only when other fields unchanged")
        void updatesStudent_onlyWhenOtherFieldsUnchanged() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent()) {
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String submitterName = bragLog.get().getSubmitterName();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, submitterName, notes);
                Integer[] expectedIds = behaviorIds.stream()
                                .map(Long::intValue).toArray(Integer[]::new);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(bragLog.get().getId()))
                        .andExpect(jsonPath("$.studentId").value(student.get().getId()))
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.behaviorIds").value(containsInAnyOrder(expectedIds)))
                        .andExpect(jsonPath("$.behaviors").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsGenerated").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.teacherName").exists())
                        .andExpect(jsonPath("$.teacherId").value(student.get().getTeacher().getId()))
                        .andExpect(jsonPath("$.grade").value(student.get().getTeacher().getGrade().name()))
                        .andExpect(jsonPath("$.submitterName").value(submitterName));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates behaviors only when other fields unchanged")
        void updatesBehaviors_onlyWhenOtherFieldsUnchanged() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String submitterName = bragLog.get().getSubmitterName();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(studentId, behaviorIds, submitterName, notes);
                Integer[] expectedIds = behaviorIds.stream()
                        .map(Long::intValue).toArray(Integer[]::new);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(bragLog.get().getId()))
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.behaviorIds").value(containsInAnyOrder(expectedIds)))
                        .andExpect(jsonPath("$.behaviors").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsGenerated").exists())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.teacherName").exists())
                        .andExpect(jsonPath("$.teacherId").value(bragLog.get().getTeacher().getId()))
                        .andExpect(jsonPath("$.grade").value(bragLog.get().getTeacher().getGrade().name()))
                        .andExpect(jsonPath("$.submitterName").value(submitterName));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates notes only when other fields unchanged")
        void updatesNotes_onlyWhenOtherFieldsUnchanged() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String newSubmitterName = bragLog.get().getSubmitterName();
                String notes = "Updated notes";
                String updateJson = buildBragLogJson(studentId, behaviorIds, newSubmitterName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.submitterName").value(newSubmitterName));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates submitter name only when other fields unchanged")
        void updatesSubmitterName_onlyWhenOtherFieldsUnchanged() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String newSubmitterName = "OnlyName Changed";
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(studentId, behaviorIds, newSubmitterName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.submitterName").value(newSubmitterName));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates submitter name to existing ADMIN user and links user")
        void updatesSubmitterNameToExistingAdminUserAndLinksUser() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<User> adminUser = userDAO.findByRole(Role.ADMIN, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && adminUser.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String adminFullName = adminUser.get().getFirstName() + " " + adminUser.get().getLastName();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(studentId, behaviorIds, adminFullName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.submitterName").value(adminFullName))
                        .andExpect(jsonPath("$.submitterUserId").value(adminUser.get().getId()));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates submitter name to existing STAFF user and links user")
        void updatesSubmitterNameToExistingStaffUserAndLinksUser() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<User> staffUser = userDAO.findByRole(Role.STAFF, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && staffUser.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String staffFullName = staffUser.get().getFirstName() + " " + staffUser.get().getLastName();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(studentId, behaviorIds, staffFullName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.submitterName").value(staffFullName))
                        .andExpect(jsonPath("$.submitterUserId").value(staffUser.get().getId()));
            }
        }

        @Test
        @Transactional
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("updates submitter name to existing TEACHER user and links user")
        void updatesSubmitterNameToExistingTeacherUserAndLinksUser() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<User> teacherUser = userDAO.findByRole(Role.TEACHER, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && teacherUser.isPresent()) {
                Long studentId = bragLog.get().getStudent().getId();
                Set<Long> behaviorIds = bragLog.get().getBehaviors().stream()
                        .map(BehaviorType::getId).collect(Collectors.toSet());
                String teacherFullName = teacherUser.get().getFirstName() + " " + teacherUser.get().getLastName();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(studentId, behaviorIds, teacherFullName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.submitterName").value(teacherFullName))
                        .andExpect(jsonPath("$.submitterUserId").value(teacherUser.get().getId()));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 when updated submitter name is null")
        void returns400_whenUpdatedSubmitterNameIsNull() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "notes";
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, null, notes);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.submitterName")
                                .value(containsString("Submitter name is required")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 when updated submitter name is blank")
        void returns400_whenUpdatedSubmitterNameIsBlank() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(bragLog.get().getStudent().getId(), behaviorIds, "", notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.submitterName")
                                .value(containsString("Submitter name must be between 2 and 250 characters")));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 when updated submitter name has no space")
        void returns400_whenUpdatedSubmitterNameHasNoSpace() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(bragLog.get().getStudent().getId(), behaviorIds, "SingleWord", notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Submitter name must contain both first and last name")));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 when updated submitter name matches a STUDENT user")
        void returns400_whenUpdatedSubmitterNameMatchesStudent() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            Optional<User> studentUser = userDAO.findByRole(Role.STUDENT, PageRequest.of(0, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent() && studentUser.isPresent()) {
                String studentFullName = studentUser.get().getFirstName() + " " + studentUser.get().getLastName();
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(bragLog.get().getStudent().getId(), behaviorIds, studentFullName, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Students cannot submit brag logs")));
            }
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 404 when updating non existent brag log")
        void returns404_whenUpdatingNonExistentBragLog() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (student.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = "notes";
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 with missing student")
        void returns400_withMissingStudent() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(null, behaviorIds, VALID_SUBMITTER_NAME, notes);
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
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 404 with invalid student")
        void returns404_withInvalidStudent() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && behaviorType.isPresent()) {
                Set<Long> behaviorIds = Set.of(behaviorType.get().getId());
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(9999L, behaviorIds, VALID_SUBMITTER_NAME, notes);
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
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 with missing behavior IDs")
        void returns400_withMissingBehaviorIds() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent()) {
                Set<Long> behaviorIds = Set.of();
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
        @DisplayName("returns 400 with invalid behavior IDs")
        void returns400_withInvalidBehaviorIds() throws Exception {
            Optional<BragLog> bragLog = bragLogDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (bragLog.isPresent() && student.isPresent()) {
                Set<Long> behaviorIds = Set.of(9999L);
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
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
                String notes = bragLog.get().getNotes();
                String updateJson = buildBragLogJson(student.get().getId(), behaviorIds, VALID_SUBMITTER_NAME, notes);
                mockMvc.perform(put(baseUrl + "/{id}", bragLog.get().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/brags/{id} - Delete brag log")
    class DeleteBragLog {
        @Test
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
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
        @WithMockUser(roles = {"ADMIN", "TEACHER", "STAFF"})
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

    private String buildBragLogJson(Long studentId, Set<Long> behaviorIds, String submitterName, String notes) {
        String submitterNameJson = submitterName == null ? "null" : "\"" + submitterName + "\"";
        String notesJson = notes == null ? "null" : "\"" + notes + "\"";
        return """
                {
                    "studentId": %s,
                    "behaviorIds": %s,
                    "submitterName": %s,
                    "notes": %s
                }
                """.formatted(studentId, behaviorIds, submitterNameJson, notesJson);
    }
}
