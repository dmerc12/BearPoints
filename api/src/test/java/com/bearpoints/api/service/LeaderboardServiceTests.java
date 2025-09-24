package com.bearpoints.api.service;

import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LeaderboardService} implementation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Service implementation and functionality</li>
 * </ul>
 * <p>Timeframes available and tested:
 * <ul>
 *     <li>WEEK</li>
 *     <li>MONTH</li>
 *     <li>SEMESTER</li>
 *     <li>YEAR</li>
 * </ul>
 *
 * @see LeaderboardService
 * @see LeaderboardServiceImpl
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class LeaderboardServiceTests {
    /** Mock brag log repository */
    @Mock
    private BragLogDAO bragLogRepository;

    /** Injects mock repository into service implementation */
    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    /** Test data */
    private BragLog bragLog;

    private BragLog createValidBragLog(LocalDateTime timestamp) {
        // Create teacher
        User teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setEmail("valid.teacher@okcps.org");
        teacherUser.setFirstName("ValidFirstName");
        teacherUser.setLastName("ValidLastName");
        teacherUser.setRole(Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setId(2L);
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.PRE_K);
        // Create student
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("valid.student@okcps.org");
        studentUser.setFirstName("ValidFirstName");
        studentUser.setLastName("ValidLastName");
        studentUser.setRole(Role.STUDENT);
        Student student = new Student();
        student.setId(1L);
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        // Create behavior type
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(1L);
        behaviorType.setName("valid behavior type");
        // Create brag log
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(Set.of(behaviorType));
        bragLog.setPointsGenerated(behaviorType.getPointValue());
        bragLog.setTimestamp(timestamp);
        bragLog.setNotes("test notes");
        return bragLog;
    }

    /** Test get leaderboard with week timeframe */
    @Test
    @DisplayName("Leaderboard with week timeframe")
    public void leaderboardWithWeekTimeframe() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        bragLog = createValidBragLog(now.minusDays(3));
        LocalDateTime startDate = now.minusWeeks(1);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(bragLog));
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, pageable);
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(LeaderboardEntryDTO::getPoints)
                .isEqualTo(bragLog.getPointsGenerated());
    }

    /** Test get leaderboard with month timeframe */
    @Test
    @DisplayName("Leaderboard with month timeframe")
    public void leaderboardWithMonthTimeframe() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        bragLog = createValidBragLog(now.minusWeeks(3));
        LocalDateTime startDate = now.minusMonths(1);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(bragLog));
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.MONTH, pageable);
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(LeaderboardEntryDTO::getPoints)
                .isEqualTo(bragLog.getPointsGenerated());
    }

    /** Test get leaderboard with semester timeframe */
    @Test
    @DisplayName("Leaderboard with semester timeframe")
    public void leaderboardWithSemesterTimeframe() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        bragLog = createValidBragLog(now.minusMonths(4));
        LocalDateTime startDate = now.minusMonths(6);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(bragLog));
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.SEMESTER, pageable);
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(LeaderboardEntryDTO::getPoints)
                .isEqualTo(bragLog.getPointsGenerated());
    }

    /** Test get leaderboard with year timeframe */
    @Test
    @DisplayName("Leaderboard with year timeframe")
    public void leaderboardWithYearTimeframe() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        bragLog = createValidBragLog(now.minusMonths(8));
        LocalDateTime startDate = now.minusYears(1);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(bragLog));
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.YEAR, pageable);
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(LeaderboardEntryDTO::getPoints)
                .isEqualTo(bragLog.getPointsGenerated());
    }

    /** Test pagination functionality */
    @Test
    @DisplayName("Pagination returns correct page")
    public void paginationReturnsCorrectPage() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        BragLog log1 = createValidBragLog(now.minusDays(1));
        BragLog log2 = createValidBragLog(now.minusDays(2));
        BragLog log3 = createValidBragLog(now.minusDays(3));
        LocalDateTime startDate = now.minusWeeks(1);
        when(bragLogRepository.findByTimestampAfter(startDate))
                .thenReturn(List.of(log1, log2, log3));
        Pageable pageable = PageRequest.of(0, 2);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
    }

    /** Test pagination when page offset is beyond total entries */
    @Test
    @DisplayName("Pagination returns empty page when offset exceeds total entries")
    public void paginationReturnsEmptyPageWhenOffsetExceedsTotal() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        BragLog log1 = createValidBragLog(now.minusDays(1));
        BragLog log2 = createValidBragLog(now.minusDays(2));
        LocalDateTime startDate = now.minusWeeks(1);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(log1, log2));
        Pageable pageable = PageRequest.of(1, 2);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, pageable);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
    }

    /** Test pagination when page offset is far beyond total entries */
    @Test
    @DisplayName("Pagination returns empty page when offset far exceeds total entries")
    public void paginationReturnsEmptyPageWhenOffsetFarExceedsTotal() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        BragLog log1 = createValidBragLog(now.minusDays(1));
        LocalDateTime startDate = now.minusWeeks(1);
        when(bragLogRepository.findByTimestampAfter(startDate)).thenReturn(List.of(log1));
        Pageable pageable = PageRequest.of(10, 1);
        Page<LeaderboardEntryDTO> result = leaderboardService.getLeaderboard(Timeframe.WEEK, pageable);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.getSize()).isEqualTo(1);
    }
}
