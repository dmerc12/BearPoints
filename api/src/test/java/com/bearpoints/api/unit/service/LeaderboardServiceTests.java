package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.LeaderboardDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.LeaderboardService;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LeaderboardService} implementation.
 *
 * <p>Validates business logic for leaderboard retrieval, including timeframe calculation
 * and proper delegation to DAO layer with correct parameters.
 *
 * <p>Key test areas:
 * <ul>
 *     <li>Service method delegation to DAO with correct parameters</li>
 *     <li>Timeframe to start date conversion accuracy</li>
 *     <li>Filter parameter propagation (teacherId, grade)</li>
 *     <li>Integration point verification between service and DAO layers</li>
 * </ul>
 *
 * <p>Timeframes calculations tested:
 * <ul>
 *     <li>WEEK - 7 days prior to current timestamp</li>
 *     <li>MONTH - 1 month prior to current timestamp</li>
 *     <li>SEMESTER - 6 months prior to current timestamp</li>
 *     <li>YEAR - 365 days prior to current timestamp</li>
 * </ul>
 *
 * @see LeaderboardService
 * @see LeaderboardServiceImpl
 * @since 1.0
 * @version 2.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class LeaderboardServiceTests {
    /** Mock DAO for verifying service-to-DAO interactions */
    @Mock
    private LeaderboardDAO leaderboardDAO;

    /** Service implementation with mocked DAO */
    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    /**
     * Tests service method delegation with basic parameters.
     *
     * <p>Verifies that service:
     * <ul>
     *     <li>Calls DAO with correct start date calculated from WEEK timeframe</li>
     *     <li>Passes through null filter parameters unchanged</li>
     *     <li>Returns DAO results without modification</li>
     *     <li>Uses truncated timestamps for consistent time handling</li>
     * </ul>
     */
    @Test
    @DisplayName("Service delegates to DAO with correct parameters")
    void getLeaderboard_DelegatesToDAO() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime expectedStartDate = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .minusWeeks(1);
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(any(LocalDateTime.class), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, null, pageable);
    }

    /**
     * Tests WEEK timeframe start date calculation.
     *
     * <p>Ensures start date is exactly 7 days prior to current timestamp,
     * truncated to seconds for consistent database comparison.
     * Uses {@link ChronoUnit#SECONDS} for precision.
     */
    @Test
    @DisplayName("Service calculates correct start date for WEEK timeframe")
    void getLeaderboard_CalculatesCorrectStartDate_Week() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusWeeks(1);
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, null, pageable);
    }

    /**
     * Tests MONTH timeframe start date calculation.
     *
     * <p>Ensures start date is exactly 30 days prior to current timestamp,
     * truncated to seconds for consistent database comparison.
     */
    @Test
    @DisplayName("Service calculates correct start date for MONTH timeframe")
    void getLeaderboard_CalculatesCorrectStartDate_Month() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusMonths(1);
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.MONTH, null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, null, pageable);
    }

    /**
     * Tests SEMESTER timeframe start date calculation.
     *
     * <p>Ensures start date is exactly 6 months prior to current timestamp,
     * truncated to seconds for consistent database comparison.
     */
    @Test
    @DisplayName("Service calculates correct start date for SEMESTER timeframe")
    void getLeaderboard_CalculatesCorrectStartDate_Semester() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusMonths(6);
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.SEMESTER, null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, null, pageable);
    }

    /**
     * Tests YEAR timeframe start date calculation.
     *
     * <p>Ensures start date is exactly 1 year prior to current timestamp,
     * truncated to seconds for consistent database comparison.
     */
    @Test
    @DisplayName("Service calculates correct start date for YEAR timeframe")
    void getLeaderboard_CalculatesCorrectStartDate_Year() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusYears(1);
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.YEAR, null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, null, pageable);
    }

    /**
     * Tests service delegation with teacher filter.
     *
     * <p>Validates that teacherId parameter is properly propagated from service layer
     * to DAO layer without modification.
     */
    @Test
    @DisplayName("Service delegates to DAO with teacher filter")
    void getLeaderboard_WithTeacherFilter_DelegatesToDAO() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusWeeks(1);
        Long teacherId = 1L;
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(teacherId, "Jane", "Smith"),
                        GradeLevel.FIRST, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(teacherId), eq(null), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, teacherId, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, teacherId, null, pageable);
    }

    /**
     * Tests service delegation with grade filter.
     *
     * <p>Validates that grade parameter is properly propagated from service layer
     * to DAO layer without modification.
     */
    @Test
    @DisplayName("Service delegates to DAO with grade filter")
    void getLeaderboard_WithGradeFilter_DelegatesToDAO() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expectedStartDate = now.minusWeeks(1);
        GradeLevel grade = GradeLevel.FIRST;
        List<LeaderboardEntryDTO> expectedContent = List.of(
                new LeaderboardEntryDTO(1,
                        new PersonDTO(1L, "John", "Doe"),
                        new PersonDTO(1L, "Jane", "Smith"),
                        grade, 100)
        );
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
        when(leaderboardDAO.findRankedLeaderboard(eq(expectedStartDate), eq(null), eq(grade), eq(pageable)))
                .thenReturn(expectedPage);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, null, grade, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(leaderboardDAO).findRankedLeaderboard(expectedStartDate, null, grade, pageable);
    }
}
