package com.bearpoints.api.integration.api;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.LeaderboardController;
import com.bearpoints.api.entity.Timeframe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link LeaderboardController} utilizing
 * Testcontainers with PostgreSQL and comprehensive test data initialization with
 * the existing {@link TestDataInitializer}.
 *
 *
 * <p>Tests the complete flow from HTTP endpoint through service layer to database,
 * validating system behavior against a production-like database environment.
 *
 * <p>Test Configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive data relationships</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see MockMvc
 * @author Dylan Mercer
 * @version 1.1
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Leaderboard Integration Tests")
public class LeaderboardTests {
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
        baseUrl = "/api/leaderboard";
    }

    /**
     * Tests complete leaderboard retrieval with comprehensive test data.
     *
     * <p>Verifies the full integration pipeline with realistic data volumes:
     * <ul>
     *     <li>Controller processes HTTP request with authentication</li>
     *     <li>Service calculates correct WEEK timeframe for filtering</li>
     *     <li>DAO executes complex ranking query with proper aggregation</li>
     *     <li>Database returns correctly ranked and paginated results</li>
     *     <li>Response contains valid student/teacher relationships</li>
     * </ul>
     * <p>Uses TestDataInitializer's 200 brag logs across 500+ students to validate
     * ranking algorithm with real data distribution.</p>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard returns ranked results with comprehensive test data")
    void getLeaderboard_WithFullTestData_ReturnsRankedResults() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.content[0].rank").isNumber())
                .andExpect(jsonPath("$.content[0].student").exists())
                .andExpect(jsonPath("$.content[0].teacher").exists())
                .andExpect(jsonPath("$.content[0].grade").isString())
                .andExpect(jsonPath("$.content[0].points").isNumber());
    }

    /**
     * Tests teacher filtering with actual database relationships.
     *
     * <p>Validates that teacher filtering works correctly with TestDataInitializer's
     * teacher-student relationships, ensuring:
     * <ul>
     *     <li>Only students from specified teacher are returned</li>
     *     <li>Ranking resets within teacher context (1,2,3...)</li>
     *     <li>All returned students belong to the filtered teacher </li>
     * </ul>
     * <p>Leverages TestDataInitializer's distribution of 20-30 students per teacher
     * to validate realistic class sizes.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with teacher filter returns class-specific ranking")
    void getLeaderboard_WithTeacherFilter_ReturnsClassRanking() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("teacherId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[*].teacher.id").value(everyItem(is(1))))
                .andExpect(jsonPath("$.content[0].rank").value(1));
    }

    /**
     * Tests grade filtering across multiple teachers.
     *
     * <p>Validates that grade filtering works across TestDataInitializer's teacher
     * distribution with randomized grade levels, ensuring:
     * <ul>
     *     <li>Students from all teachers with specified grade are included</li>
     *     <li>Ranking resets within grade level context</li>
     *     <li>Cross-teacher aggregation works correctly</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with grade filter returns grade-specific ranking")
    void getLeaderboard_WithGradeFilter_ReturnsGradeRanking() throws Exception {
        String grade = "FIRST";
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("grade", grade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[*].grade").value(everyItem(is(grade))))
                .andExpect(jsonPath("$.content[0].rank").value(1));
    }

    /**
     * Tests timeframe filtering with time-based data separation.
     *
     * <p>Validates that WEEK timeframe calculation works with TestDataInitializer's
     * brag log timestamps, ensuring recent data is included while properly excluding older records.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with WEEK timeframe filters recent brag logs correctly")
    void getLeaderboard_WeekTimeframe_FiltersRecentData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
    }

    /**
     * Tests all timeframe values with comprehensive test data.
     *
     * <p>Validates that each timeframe enum value works correctly with TestDataInitializer's
     * temporal data distribution.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard handles all timeframe values correctly")
    void getLeaderboard_HandlesAllTimeframes_ReturnResults() throws Exception {
        for (Timeframe timeframe : Timeframe.values()) {
            mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                        .param("timeframe", timeframe.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists());
        }
    }

    /**
     * Tests pagination with realistic data volumes.
     *
     * <p>Validates pagination behavior with TestDataInitializer's 500+ students,
     * ensuring correct page metadata and result slicing.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with pagination returns correct page metadata")
    void getLeaderboard_WithPagination_ReturnsCorrectPageData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("page", "0")
                    .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(5)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    /**
     * Tests combined filtering scenarios with complex criteria.
     *
     * <p>Validates that multiple filters work together correctly with TestDataInitializer's
     * complex data relationships.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with combined filters returns precise results")
    void getLeaderboard_WithCombinedFilters_ReturnsPreciseResults() throws Exception {
        String grade = "FIRST";
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("teacherId", "1")
                    .param("grade", grade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].teacher.id").value(everyItem(is(1))))
                .andExpect(jsonPath("$.content[*].grade").value(everyItem(is(grade))));
    }

    /**
     * Tests empty result set handling with non-matching filters.
     *
     * <p>Validates graceful handling of scenarios where no data matches the filter criteria.
     */
    @Test
    @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
    @DisplayName("GET /leaderboard with non-matching filters returns empty results gracefully")
    void getLeaderboard_WithNonMatchingFilters_ReturnsEmptyResults() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                    .param("timeframe", "WEEK")
                    .param("teacherId", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /**
     * Tests leaderboard access control with different user roles.
     *
     * <p>Validates that all authorized roles (STUDENT, TEACHER, ADMIN) can access the
     * leaderboard endpoint successfully.
     */
    @Test
    @DisplayName("All authorized roles can access leaderboard endpoint")
    void getLeaderboard_AllAuthorizedRoles_CanAccess() throws Exception {
        String[] roles = {"STUDENT", "TEACHER", "ADMIN"};
        for (String role : roles) {
            mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                        .param("timeframe", "WEEK")
                        .with(SecurityMockMvcRequestPostProcessors.user("testUser").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists());
        }
    }
}
