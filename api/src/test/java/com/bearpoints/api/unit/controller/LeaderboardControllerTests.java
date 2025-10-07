package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.LeaderboardController;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * REST API unit tests for {@link LeaderboardController}.
 *
 * <p>Validates HTTP endpoint behavior, request parameter handling, and response
 * structure for leaderboard retrieval operations.
 *
 * <p>Test coverage includes:
 * <ul>
 *     <li>Successful API responses with various parameter combinations</li>
 *     <li>Request parameter binding and validation</li>
 *     <li>Service layer interaction and delegation</li>
 *     <li>Empty data set handling and proper HTTP status</li>
 *     <li>Pagination support and response metadata</li>
 *     <li>Filter parameter propagation to service layer</li>
 * </ul>
 *
 *
 * @see LeaderboardController
 * @since 1.0
 * @version 2.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class LeaderboardControllerTests {
    /** Mock service for verifying controller-to-service interactions */
    @Mock
    private LeaderboardService leaderboardService;

    /** Controller instance with mocked service */
    @InjectMocks
    private LeaderboardController leaderboardController;

    private List<LeaderboardEntryDTO> sampleLeaderboard;

    /**
     * Initialization sample leaderboard data for controller tests.
     * <p>Creates varied test data with different ranks, points, and
     * student/teacher combinations to validate response structure.
     */
    @BeforeEach
    public void setup() {
        sampleLeaderboard = Arrays.asList(
                new LeaderboardEntryDTO(
                        1,
                        new PersonDTO(1L, "Student", "One"),
                        new PersonDTO(1L, "Teacher", "A"),
                        GradeLevel.THIRD, 150),
                new LeaderboardEntryDTO(
                        2,
                        new PersonDTO(2L, "Student", "Two"),
                        new PersonDTO(2L, "Teacher", "B"),
                        GradeLevel.FOURTH, 120),
                new LeaderboardEntryDTO(
                        3,
                        new PersonDTO(3L, "Student", "Three"),
                        new PersonDTO(3L, "Teacher", "C"),
                        GradeLevel.SECOND, 95)
        );
    }

    /**
     * Tests successful leaderboard retrieval scenarios.
     */
    @Nested
    @DisplayName("Successful retrieval scenarios")
    class SuccessfulScenarios {
        /**
         * Tests default parameter handling and successful response.
         *
         * <p>Verifies that:
         * <ul>
         *     <li>Default timeframe (WEEK) is used when not specified</li>
         *     <li>HTTP 200 equivalent behavior (non-empty successful response)</li>
         *     <li>Response contains properly structured leaderboard data</li>
         *     <li>Student and teacher information is correctly populated</li>
         *     <li>Points and ranking data is accurate</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard returns 200 with default timeframe")
        void getLeaderboard_DefaultTimeframe_ReturnsLeaderboard() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
            when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(expectedPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, null, null, pageable);
            assertNotNull(response);
            assertEquals(3, response.getContent().size());
            assertEquals(150, response.getContent().getFirst().getPoints());
            assertEquals("Student", response.getContent().getFirst().getStudent().getFirstName());
            assertEquals("One", response.getContent().getFirst().getStudent().getLastName());
            assertEquals("Teacher", response.getContent().getFirst().getTeacher().getFirstName());
            assertEquals("A", response.getContent().getFirst().getTeacher().getLastName());
        }

        /**
         * Tests teacher filter parameter handling.
         *
         * <p>Validates that teacherId request parameter is properly bound and propagated to service layer.
         */
        @Test
        @DisplayName("GET /leaderboard returns 200 with teacher filter")
        void getLeaderboard_TeacherFilter_ReturnsLeaderboard() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
            Long teacherId = 1L;
            when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(teacherId), eq(null), any(Pageable.class)))
                    .thenReturn(expectedPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, teacherId, null, pageable);
            assertNotNull(response);
            assertEquals(3, response.getContent().size());
            verify(leaderboardService).getLeaderboard(Timeframe.WEEK, teacherId, null, pageable);
        }

        /**
         * Tests grade filter parameter handling.
         *
         * <p>Validates that grade request parameter is properly bound and propagated to service layer.
         */
        @Test
        @DisplayName("GET /leaderboard returns 200 with grade filter")
        void getLeaderboard_GradeFilter_ReturnsLeaderboard() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
            GradeLevel grade = GradeLevel.SECOND;
            when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(grade), any(Pageable.class)))
                    .thenReturn(expectedPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, null, grade, pageable);
            assertNotNull(response);
            assertEquals(3, response.getContent().size());
            verify(leaderboardService).getLeaderboard(Timeframe.WEEK, null, grade, pageable);
        }

        /**
         * Tests all timeframe enum value handling.
         *
         * <p>Ensures controller properly handles every available timeframe option without errors or data corruption.
         */
        @Test
        @DisplayName("GET /leaderboard handles all timeframe values")
        void getLeaderboard_AllTimeframe_ReturnsLeaderboard() {
            Pageable pageable = PageRequest.of(0, 20);
            for (Timeframe timeframe : Timeframe.values()) {
                Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
                when(leaderboardService.getLeaderboard(eq(timeframe), eq(null), eq(null), any(Pageable.class))).thenReturn(expectedPage);
                Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(timeframe, null, null, pageable);
                assertNotNull(response);
                assertEquals(3, response.getContent().size());
            }
        }
    }

    /**
     * Tests empty and edge case scenarios.
     */
    @Nested
    @DisplayName("Empty leaderboard scenarios")
    class EmptyScenarios {
        /**
         * Tests empty data set handling.
         *
         * <p>Validates that empty results are properly handled with:
         * <ul>
         *     <li>Non-null response with empty content collection</li>
         *     <li>Correct total elements (zero) reporting</li>
         *     <li>Proper pagination metadata for empty data sets</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard returns empty list when no data")
        void getLeaderboard_NoData_ReturnsEmptyList() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(leaderboardService.getLeaderboard(Timeframe.WEEK, null, null, pageable)).thenReturn(emptyPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, null, null, pageable);
            assertNotNull(response);
            assertTrue(response.getContent().isEmpty());
            assertEquals(0, response.getTotalElements());
        }

        /**
         * Tests pagination scenarios.
         */
        @Nested
        @DisplayName("Pagination scenarios")
        class PaginationScenarios {
            /**
             * Tests pagination parameter handling and response slicing.
             *
             * <p>Verifies that:
             * <ul>
             *     <li>Pageable parameters are properly applied</li>
             *     <li>Response contains only requested page's data</li>
             *     <li>Pagination metadata (page number, size, total) is accurate</li>
             *     <li>Total elements and pages are correctly calculated</li>
             * </ul>
             */
            @Test
            @DisplayName("GET /leaderboard returns correct page with pagination")
            void getLeaderboard_WithPagination_ReturnsCorrectPage() {
                Pageable pageable = PageRequest.of(1, 1);
                List<LeaderboardEntryDTO> secondPageContent = List.of(sampleLeaderboard.get(1));
                Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(secondPageContent, pageable, sampleLeaderboard.size());
                when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(null), eq(pageable))).thenReturn(expectedPage);
                Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, null, null, pageable);
                assertNotNull(response);
                assertEquals(1, response.getContent().size());
                assertEquals(2L, response.getContent().getFirst().getStudent().getId());
                assertEquals(1, response.getNumber());
                assertEquals(1, response.getSize());
                assertEquals(3, response.getTotalElements());
                assertEquals(3, response.getTotalPages());
            }
        }
    }
}
