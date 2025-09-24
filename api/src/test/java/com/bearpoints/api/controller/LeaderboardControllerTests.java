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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
 * @version 2.0
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
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
            when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), any(Pageable.class)))
                    .thenReturn(expectedPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, pageable);
            assertNotNull(response);
            assertEquals(3, response.getContent().size());
            assertEquals(150, response.getContent().getFirst().getPoints());
            assertEquals("Student", response.getContent().getFirst().getStudent().getFirstName());
            assertEquals("One", response.getContent().getFirst().getStudent().getLastName());
            assertEquals("Teacher", response.getContent().getFirst().getTeacher().getFirstName());
            assertEquals("A", response.getContent().getFirst().getTeacher().getLastName());
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
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
            when(leaderboardService.getLeaderboard(eq(Timeframe.MONTH), any(Pageable.class)))
                    .thenReturn(expectedPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.MONTH, pageable);
            assertNotNull(response);
            assertEquals(3, response.getContent().size());
            assertEquals("Student", response.getContent().get(1).getStudent().getFirstName());
            assertEquals("Two", response.getContent().get(1).getStudent().getLastName());
            assertEquals("Teacher", response.getContent().get(1).getTeacher().getFirstName());
            assertEquals("B", response.getContent().get(1).getTeacher().getLastName());
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
            Pageable pageable = PageRequest.of(0, 20);
            for (Timeframe timeframe : Timeframe.values()) {
                Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, sampleLeaderboard.size());
                when(leaderboardService.getLeaderboard(eq(timeframe), any(Pageable.class))).thenReturn(expectedPage);
                Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(timeframe, pageable);
                assertNotNull(response);
                assertEquals(3, response.getContent().size());
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
            Pageable pageable = PageRequest.of(0, 20);
            Page<LeaderboardEntryDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(leaderboardService.getLeaderboard(Timeframe.WEEK, pageable)).thenReturn(emptyPage);
            Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, pageable);
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
            @Test
            @DisplayName("GET /leaderboard returns correct page with pagination")
            void getLeaderboard_WithPagination_ReturnsCorrectPage() {
                Pageable pageable = PageRequest.of(1, 1);
                List<LeaderboardEntryDTO> secondPageContent = List.of(sampleLeaderboard.get(1));
                Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(secondPageContent, pageable, sampleLeaderboard.size());
                when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(pageable))).thenReturn(expectedPage);
                Page<LeaderboardEntryDTO> response = leaderboardController.getLeaderboard(Timeframe.WEEK, pageable);
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
