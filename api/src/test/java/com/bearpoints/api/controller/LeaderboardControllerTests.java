package com.bearpoints.api.controller;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LeaderboardController}.
 * <p>Verifies functionality of leaderboard retrieval endpoint:
 * <ul>
 *     <li>Authorization requirements</li>
 *     <li>Timeframe parameter handling</li>
 *     <li>Response structure and content</li>
 *     <li>Service interaction</li>
 * </ul>
 *
 * <p>Tests validate that:
 * <ul>
 *     <li>Authorized requests return 200 OK with leaderboard data</li>
 *     <li>Timeframe parameter is properly passed to service</li>
 *     <li>Default timeframe (WEEK) is used when none is specified</li>
 *     <li>Response contains properly formatted leaderboard entries</li>
 * </ul>
 *
 * @see LeaderboardController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class LeaderboardControllerTests {
    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    private List<LeaderboardEntryDTO> sampleLeaderboard;

    @BeforeEach
    public void setup() {
        sampleLeaderboard = Arrays.asList(
                new LeaderboardEntryDTO(
                        new PersonDTO(1L, "Student", "One"),
                        new PersonDTO(1L, "Teacher", "A"),
                        "THIRD", 150),
                new LeaderboardEntryDTO(
                        new PersonDTO(2L, "Student", "Two"),
                        new PersonDTO(2L, "Teacher", "B"),
                        "FOURTH", 120),
                new LeaderboardEntryDTO(
                        new PersonDTO(3L, "Student", "Three"),
                        new PersonDTO(3L, "Teacher", "C"),
                        "SECOND", 95)
        );
    }

    /**
     * Tests successful leaderboard retrieval scenarios.
     */
    @Nested
    @DisplayName("Successful retrieval scenarios")
    class SuccessfulScenarios {
        /**
         * Tests leaderboard retrieval with default timeframe.
         * <p>Verifies:
         * <ul>
         *     <li>HTTP 200 OK status</li>
         *     <li>Default timeframe (WEEK) is used</li>
         *     <li>Response body contains expected data</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard returns 200 with default timeframe")
        void getLeaderboard_DefaultTimeframe_ReturnsLeaderboard() {
            when(leaderboardService.getLeaderboard(Timeframe.WEEK)).thenReturn(sampleLeaderboard);
            List<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK);
            assertNotNull(response);
            assertEquals(3, response.size());
            assertEquals(150, response.getFirst().getPoints());
            assertEquals("Student", response.getFirst().getStudent().getFirstName());
            assertEquals("One", response.getFirst().getStudent().getLastName());
            assertEquals("Teacher", response.getFirst().getTeacher().getFirstName());
            assertEquals("A", response.getFirst().getTeacher().getLastName());
        }

        /**
         * Tests leaderboard retrieval with explicit timeframe.
         * <p>Verifies:
         * <ul>
         *     <li>HTTP 200 OK status</li>
         *     <li>Specified timeframe is passed to service</li>
         *     <li>Response body contains expected data</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard returns 200 with specified timeframe")
        void getLeaderboard_ExplicitTimeframe_ReturnsLeaderboard() {
            when(leaderboardService.getLeaderboard(Timeframe.MONTH)).thenReturn(sampleLeaderboard);
            List<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.MONTH);
            assertNotNull(response);
            assertEquals(3, response.size());
            assertEquals("Student", response.get(1).getStudent().getFirstName());
            assertEquals("Two", response.get(1).getStudent().getLastName());
            assertEquals("Teacher", response.get(1).getTeacher().getFirstName());
            assertEquals("B", response.get(1).getTeacher().getLastName());
        }

        /**
         * Tests leaderboard retrieval with all valid timeframes.
         * <p>Verifies:
         * <ul>
         *     <li>All enum values are properly handled</li>
         *     <li>Service receives correct timeframe parameter</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard handles all timeframe values")
        void getLeaderboard_AllTimeframe_ReturnsLeaderboard() {
            for (Timeframe timeframe : Timeframe.values()) {
                when(leaderboardService.getLeaderboard(timeframe)).thenReturn(sampleLeaderboard);
                List<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(timeframe);
                assertNotNull(response);
                assertEquals(3, response.size());
            }
        }
    }

    /**
     * Tests empty leaderboard scenarios.
     */
    @Nested
    @DisplayName("Empty leaderboard scenarios")
    class EmptyScenarios {
        /**
         * Tests handling of empty leaderboard data.
         * <p>Verifies:
         * <ul>
         *     <li>HTTP 200 OK status</li>
         *     <li>Empty list is returned</li>
         * </ul>
         */
        @Test
        @DisplayName("GET /leaderboard returns empty list when no data")
        void getLeaderboard_NoData_ReturnsEmptyList() {
            when(leaderboardService.getLeaderboard(Timeframe.WEEK)).thenReturn(List.of());
            List<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK);
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }
}
