package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.StudentRewardController;
import com.bearpoints.api.dao.*;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link StudentRewardController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete student reward management flow from HTTP endpoint through service layer to
 * database, validating system behavior against production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive student reward data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 1.2
 * @author Dylan Mercer
 */
@DisplayName("Student Reward Integration Tests")
public class StudentRewardTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private RewardItemDAO rewardItemDAO;

    @Autowired
    private StudentRewardDAO studentRewardDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/rewards";
    }

    @Nested
    @DisplayName("GET /api/rewards - Retrieve student rewards")
    class GetAllStudentRewards {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns paginated student rewards with default parameters")
        void returnsPaginatedStudentRewardsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedStudentRewards() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "studentName,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns empty page when no student rewards exist")
        void returnsEmptyPageWhenNoStudentRewardsExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/rewards/search - Search student rewards")
    class SearchStudentRewards {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("no criteria returns all student rewards")
        void noCriteriaReturnsAllStudentRewards() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by student name returns matching student rewards")
        void byStudentNameCriteriaReturnsMatchingStudentRewards() throws Exception {
            String searchTerm = "S";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].student.firstName",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("empty student name is ignored and returns all")
        void emptyStudentNameIsIgnored() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                        .param("studentName", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("blank student name is ignored and returns all")
        void blankStudentNameIsIgnored() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", "     "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by student ID returns matching student rewards")
        void byStudentIdCriteriaReturnsMatchingStudentRewards() throws Exception {
            Long studentId = 1L;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentId", String.valueOf(studentId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].student.id",
                            everyItem(equalTo(studentId))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by item name returns matching student rewards")
        void byItemNameCriteriaReturnsMatchingStudentRewards() throws Exception {
            String searchTerm = "S";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].rewardItem.name",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("empty item name is ignored and returns all student rewards")
        void emptyItemNameCriteriaIgnoredAndReturnsAllStudentRewards() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by item ID returns matching student rewards")
        void byItemIdCriteriaReturnsMatchingStudentRewards() throws Exception {
            Long itemId = 1L;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("itemId", String.valueOf(itemId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].rewardItem.id",
                            everyItem(equalTo(itemId))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by points used range returns matching student rewards")
        void byPointsUsedRangeCriteriaReturnsMatchingStudentRewards() throws Exception {
            Integer minPointsUsed = 5;
            Integer maxPointsUsed = 15;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointsUsed", String.valueOf(minPointsUsed))
                            .param("maxPointsUsed", String.valueOf(maxPointsUsed)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(greaterThanOrEqualTo(minPointsUsed))))
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(lessThanOrEqualTo(maxPointsUsed))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by min points used returns matching student rewards")
        void byMinPointsUsedCriteriaReturnsMatchingStudentRewards() throws Exception {
            Integer minPointsUsed = 10;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointsUsed", String.valueOf(minPointsUsed)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(greaterThanOrEqualTo(minPointsUsed))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by max points used returns matching student rewards")
        void byMaxPointsUsedCriteriaReturnsMatchingStudentRewards() throws Exception {
            Integer maxPointsUsed = 10;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxPointsUsed", String.valueOf(maxPointsUsed)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(lessThanOrEqualTo(maxPointsUsed))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by redeemed during date range returns matching student rewards")
        void byRedeemedDuringDateRangeCriteriaReturnsMatchingStudentRewards() throws Exception {
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endDate = LocalDateTime.now();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("startDate", String.valueOf(startDate))
                            .param("endDate", String.valueOf(endDate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(greaterThanOrEqualTo(startDate))))
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(lessThanOrEqualTo(endDate))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by redeemed after start date only returns matching student rewards")
        void byRedeemedAfterStartDateCriteriaReturnsMatchingStudentRewards() throws Exception {
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            mockMvc.perform(get(baseUrl + "/search")
                            .param("startDate", String.valueOf(startDate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(greaterThanOrEqualTo(startDate))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by redeemed before end date only returns matching student rewards")
        void byRedeemedBeforeEndDateCriteriaReturnsMatchingStudentRewards() throws Exception {
            LocalDateTime endDate = LocalDateTime.now();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("endDate", String.valueOf(endDate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(lessThanOrEqualTo(endDate))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with combined criteria returns matching student rewards")
        void withCombinedCriteriaReturnsMatchingStudentRewards() throws Exception {
            String studentName = "S";
            Long studentId = 1L;
            String itemName = "S";
            Long itemId = 1L;
            Integer minPointsUsed = 5;
            Integer maxPointsUsed = 15;
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endDate = LocalDateTime.now();
            mockMvc.perform(get(baseUrl + "/search")
                            .param("studentName", studentName)
                            .param("studentId", String.valueOf(studentId))
                            .param("itemName", itemName)
                            .param("itemId", String.valueOf(itemId))
                            .param("minPointsUsed", String.valueOf(minPointsUsed))
                            .param("maxPointsUsed", String.valueOf(maxPointsUsed))
                            .param("startDate", String.valueOf(startDate))
                            .param("endDate", String.valueOf(endDate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].student.firstName",
                            everyItem(containsStringIgnoringCase(studentName))))
                    .andExpect(jsonPath("$.content[*].student.id",
                            everyItem(equalTo(studentId))))
                    .andExpect(jsonPath("$.content[*].rewardItem.name",
                            everyItem(containsStringIgnoringCase(itemName))))
                    .andExpect(jsonPath("$.content[*].rewardItem.id",
                            everyItem(equalTo(itemId))))
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(greaterThanOrEqualTo(minPointsUsed))))
                    .andExpect(jsonPath("$.content[*].rewardItem.pointCost",
                            everyItem(lessThanOrEqualTo(maxPointsUsed))))
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(greaterThanOrEqualTo(startDate))))
                    .andExpect(jsonPath("$.content[*].redeemedAt",
                            everyItem(lessThanOrEqualTo(endDate))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with non-matching criteria returns empty results")
        void withNonMatchingCriteriaReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("itemName", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResultsWhenSortParameterProvided() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("itemName", "")
                            .param("sort", "redeemedAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/rewards/{id} - Get student reward by ID")
    class GetStudentRewardById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns student reward when ID exists")
        void returnsStudentRewardWhenIdExists() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                mockMvc.perform(get(baseUrl + "/{id}", studentReward.get().getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentReward.get().getId()))
                        .andExpect(jsonPath("$.studentId").exists())
                        .andExpect(jsonPath("$.studentName").exists())
                        .andExpect(jsonPath("$.itemId").exists())
                        .andExpect(jsonPath("$.itemName").exists())
                        .andExpect(jsonPath("$.pointsUsed").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404WhenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Student reward not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/rewards - Create student reward")
    class CreateStudentReward {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("creates student reward with valid data")
        void createStudentRewardWithValidData() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && rewardItem.isPresent()) {
                Long studentId = student.get().getId();
                String studentName = student.get().getUser().getFirstName() + " " + student.get().getUser().getLastName();
                Long itemId = rewardItem.get().getId();
                String itemName = rewardItem.get().getName();
                String createJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
                                .with(csrf()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").value(studentName))
                        .andExpect(jsonPath("$.itemId").value(itemId))
                        .andExpect(jsonPath("$.itemName").value(itemName))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsUsed").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 when student has insufficient points")
        void returns400WithInsufficientPoints() throws Exception {
            Teacher teacher = new Teacher();
            teacher.setGrade(GradeLevel.FIRST);
            teacher.setUser(createMinimalUser("teacher@okcps.org", "Some", "Teacher", Role.TEACHER));
            Teacher savedTeacher = teacherDAO.save(teacher);
            Student student = new Student();
            student.setPoints(5);
            student.setUser(createMinimalUser("lowpoints@okcps.org", "Low", "Points", Role.STUDENT));
            student.generateToken();
            student.setTeacher(savedTeacher);
            Student savedStudent = studentDAO.save(student);
            RewardItem item = new RewardItem();
            item.setName("Expensive Item");
            item.setPointCost(10);
            item.setStock(100);
            RewardItem savedItem = rewardItemDAO.save(item);
            String createJSON = buildStudentRewardJson(savedStudent.getId(), savedItem.getId());
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJSON)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Insufficient points to redeem this reward"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 when reward item has insufficient stock")
        void returns400WithInsufficientStock() throws Exception {
            Teacher teacher = new Teacher();
            teacher.setGrade(GradeLevel.FIRST);
            teacher.setUser(createMinimalUser("other-teacher@okcps.org", "Some", "Teacher", Role.TEACHER));
            Teacher savedTeacher = teacherDAO.save(teacher);
            Student student = new Student();
            student.setPoints(100);
            student.setUser(createMinimalUser("enoughpoints@okcps.org", "Enough", "Points", Role.STUDENT));
            student.generateToken();
            student.setTeacher(savedTeacher);
            Student savedStudent = studentDAO.save(student);
            RewardItem item = new RewardItem();
            item.setName("Out of Stock Item");
            item.setPointCost(5);
            item.setStock(0);
            RewardItem savedItem = rewardItemDAO.save(item);
            String createJSON = buildStudentRewardJson(savedStudent.getId(), savedItem.getId());
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJSON)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Insufficient stock to redeem this reward"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 with invalid student")
        void returns404WithInvalidStudent() throws Exception {
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (rewardItem.isPresent()) {
                Long studentId = 9999L;
                Long itemId = rewardItem.get().getId();
                String createJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Student not found with ID: " + studentId)))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 with invalid reward item")
        void Returns404WithInvalidRewardItem() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Long studentId = student.get().getId();
                Long itemId = 9999L;
                String createJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Reward item not found with ID: " + itemId)))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with missing student")
        void returns400WithMissingStudent() throws Exception {
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (rewardItem.isPresent()) {
                Long itemId = rewardItem.get().getId();
                String createJSON = buildStudentRewardJson(null, itemId);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
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
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with missing reward item")
        void Returns400WithMissingRewardItem() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent()) {
                Long studentId = student.get().getId();
                String createJSON = buildStudentRewardJson(studentId, null);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.itemId")
                                .value(containsString("Item ID is required")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when user is not ADMIN")
        void returns403WhenUserIsNotAdmin() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && rewardItem.isPresent()) {
                Long studentId = student.get().getId();
                Long itemId = rewardItem.get().getId();
                String createJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJSON)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/rewards/{id} - Update student reward")
    class UpdateStudentReward {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates a student reward with valid data")
        void updatesStudentRewardWithValidData() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && studentReward.isPresent() && rewardItem.isPresent()) {
                Long studentId = student.get().getId();
                Long studentRewardId = studentReward.get().getId();
                String studentName = student.get().getUser().getFirstName() + " " + student.get().getUser().getLastName();
                Long itemId = rewardItem.get().getId();
                String itemName = rewardItem.get().getName();
                Integer pointsUsed = rewardItem.get().getPointCost();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentRewardId))
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").value(studentName))
                        .andExpect(jsonPath("$.itemId").value(itemId))
                        .andExpect(jsonPath("$.itemName").value(itemName))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsUsed").value(pointsUsed));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates a student reward with nothing changed")
        void updatesStudentRewardWithNothingChanged() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                Long studentId = studentReward.get().getStudent().getId();
                Long studentRewardId = studentReward.get().getId();
                String studentName = studentReward.get().getStudent().getUser().getFirstName() + " " +
                        studentReward.get().getStudent().getUser().getLastName();
                Long itemId = studentReward.get().getRewardItem().getId();
                String itemName = studentReward.get().getRewardItem().getName();
                Integer pointsUsed = studentReward.get().getRewardItem().getPointCost();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentRewardId))
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").value(studentName))
                        .andExpect(jsonPath("$.itemId").value(itemId))
                        .andExpect(jsonPath("$.itemName").value(itemName))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsUsed").value(pointsUsed));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates a student reward with only student changed")
        void updatesStudentRewardWithOnlyStudentChanged() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent() && student.isPresent()) {
                Long studentId = student.get().getId();
                Long studentRewardId = studentReward.get().getId();
                String studentName = student.get().getUser().getFirstName() + " " +
                        student.get().getUser().getLastName();
                Long itemId = studentReward.get().getRewardItem().getId();
                String itemName = studentReward.get().getRewardItem().getName();
                Integer pointsUsed = studentReward.get().getRewardItem().getPointCost();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentRewardId))
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").value(studentName))
                        .andExpect(jsonPath("$.itemId").value(itemId))
                        .andExpect(jsonPath("$.itemName").value(itemName))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsUsed").value(pointsUsed));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates a student reward with only reward item changed")
        void updatesStudentRewardWithOnlyRewardItemChanged() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent() && rewardItem.isPresent()) {
                Long studentId = studentReward.get().getStudent().getId();
                Long studentRewardId = studentReward.get().getId();
                String studentName = studentReward.get().getStudent().getUser().getFirstName() + " " +
                        studentReward.get().getStudent().getUser().getLastName();
                Long itemId = rewardItem.get().getId();
                String itemName = rewardItem.get().getName();
                Integer pointsUsed = rewardItem.get().getPointCost();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(studentRewardId))
                        .andExpect(jsonPath("$.studentId").value(studentId))
                        .andExpect(jsonPath("$.studentName").value(studentName))
                        .andExpect(jsonPath("$.itemId").value(itemId))
                        .andExpect(jsonPath("$.itemName").value(itemName))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.pointsUsed").value(pointsUsed));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 when updating to a student with insufficient points")
        void returns400WithInsufficientPointsWhenChangingStudent() throws Exception {
            Teacher teacher = new Teacher();
            teacher.setGrade(GradeLevel.FIRST);
            teacher.setUser(createMinimalUser("teacher@okcps.org", "Some", "Teacher", Role.TEACHER));
            Teacher savedTeacher = teacherDAO.save(teacher);
            Student originalStudent = new Student();
            originalStudent.setPoints(100);
            originalStudent.setUser(createMinimalUser("original@okcps.org", "Original", "Student", Role.STUDENT));
            originalStudent.generateToken();
            originalStudent.setTeacher(savedTeacher);
            Student savedOriginalStudent = studentDAO.save(originalStudent);
            RewardItem item = new RewardItem();
            item.setName("Test Item");
            item.setPointCost(30);
            item.setStock(5);
            RewardItem savedItem = rewardItemDAO.save(item);
            StudentReward reward = new StudentReward();
            reward.setStudent(savedOriginalStudent);
            reward.setRewardItem(savedItem);
            StudentReward savedReward = studentRewardDAO.save(reward);
            Student newStudent = new Student();
            newStudent.setPoints(10);
            newStudent.setUser(createMinimalUser("new@okcps.org", "New", "Student", Role.STUDENT));
            newStudent.generateToken();
            newStudent.setTeacher(savedTeacher);
            Student savedNewStudent = studentDAO.save(newStudent);
            String updateJSON = buildStudentRewardJson(savedNewStudent.getId(), savedItem.getId());
            mockMvc.perform(put(baseUrl + "/{id}", savedReward.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJSON)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Insufficient points to redeem this reward"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 when updating to an item with insufficient stock")
        void returns400WithInsufficientStockWhenChangingItem() throws Exception {
            Teacher teacher = new Teacher();
            teacher.setGrade(GradeLevel.FIRST);
            teacher.setUser(createMinimalUser("other-teacher@okcps.org", "Some", "Teacher", Role.TEACHER));
            Teacher savedTeacher = teacherDAO.save(teacher);
            Student student = new Student();
            student.setPoints(100);
            student.setUser(createMinimalUser("student@okcps.org", "Some", "Student", Role.STUDENT));
            student.generateToken();
            student.setTeacher(savedTeacher);
            Student savedStudent = studentDAO.save(student);
            RewardItem originalItem = new RewardItem();
            originalItem.setName("Original Item");
            originalItem.setPointCost(20);
            originalItem.setStock(5);
            RewardItem savedOriginalItem = rewardItemDAO.save(originalItem);
            StudentReward reward = new StudentReward();
            reward.setStudent(savedStudent);
            reward.setRewardItem(savedOriginalItem);
            StudentReward savedReward = studentRewardDAO.save(reward);
            RewardItem newItem = new RewardItem();
            newItem.setName("Out of Stock Item");
            newItem.setPointCost(15);
            newItem.setStock(0);
            RewardItem savedNewItem = rewardItemDAO.save(newItem);
            String updateJSON = buildStudentRewardJson(savedStudent.getId(), savedNewItem.getId());
            mockMvc.perform(put(baseUrl + "/{id}", savedReward.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJSON)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Insufficient stock to redeem this reward"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 with invalid student")
        void returns404WithInvalidStudent() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                Long studentId = 9999L;
                Long studentRewardId = studentReward.get().getId();
                Long itemId = studentReward.get().getRewardItem().getId();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Student not found with ID: " + studentId)))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 with invalid reward item")
        void Returns404WithInvalidRewardItem() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                Long studentId = studentReward.get().getStudent().getId();
                Long studentRewardId = studentReward.get().getId();
                Long itemId = 9999L;
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Reward item not found with ID: " + itemId)))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with missing student")
        void returns400WithMissingStudent() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                Long studentRewardId = studentReward.get().getId();
                Long itemId = studentReward.get().getRewardItem().getId();
                String updateJSON = buildStudentRewardJson(null, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
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
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with missing reward item")
        void Returns400WithMissingRewardItem() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                Long studentId = studentReward.get().getStudent().getId();
                Long studentRewardId = studentReward.get().getId();
                String updateJSON = buildStudentRewardJson(studentId, null);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.fieldErrors.itemId")
                                .value(containsString("Item ID is required")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when user is not ADMIN")
        void returns403WhenUserIsNotAdmin() throws Exception {
            Optional<Student> student = studentDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> rewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (student.isPresent() && studentReward.isPresent() && rewardItem.isPresent()) {
                Long studentId = student.get().getId();
                Long studentRewardId = studentReward.get().getId();
                Long itemId = rewardItem.get().getId();
                String updateJSON = buildStudentRewardJson(studentId, itemId);
                mockMvc.perform(put(baseUrl + "/{id}", studentRewardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJSON)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/rewards/{id} - Delete student reward")
    class DeleteStudentReward {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deletes student reward and returns 204")
        void deletesStudentRewardAndReturns204() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", studentReward.get().getId())
                        .with(csrf()))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non existent student reward")
        void returns404WhenDeletingNonExistentStudentReward() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Student reward not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when user is ADMIN")
        void returns403WhenUserIsNotAdmin() throws Exception {
            Optional<StudentReward> studentReward = studentRewardDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (studentReward.isPresent()) {
                mockMvc.perform(delete(baseUrl + "/{id}", studentReward.get().getId())
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    private String buildStudentRewardJson(Long studentId, Long itemId) {
        return """
               {
                    "studentId": %s,
                    "itemId": %s
               }
               """.formatted(studentId, itemId);
    }

    private User createMinimalUser(String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userDAO.save(user);
    }
}
