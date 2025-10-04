package com.bearpoints.api.controller;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for leaderboard data retrieval.
 * <p>Provides secure access to student point leaderboards filtered by timeframe, teacher, and grade.
 *
 * <p>Endpoint:
 * <ul>
 *     <li>{@code GET /api/leaderboard} - Retrieves current leaderboard with pagination and filtering</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 *     <li>Requires STUDENT, TEACHER, or ADMIN role</li>
 *     <li>Supports timeframe filtering (WEEK, MONTH, SEMESTER, YEAR)</li>
 *     <li>Supports teacher and grade filtering</li>
 *     <li>Default timeframe is WEEK</li>
 *     <li>Returns paginated results with dynamic contextual ranking (global/class/grade ranks)</li>
 *     <li>Supports page, size, and sort parameters</li>
 * </ul>
 *
 * @see LeaderboardService
 * @see Timeframe
 * @version 3.0
 * @author Dylan Mercer
 */
@CrossOrigin
@RestController
@RequestMapping("/api/leaderboard")
@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * Retrieves current leaderboard data with pagination and filtering.
     * <p>Returns list of students with dynamic ranking based on filters.
     *
     * @param timeframe Filter period (optional, defaults to WEEK)
     * @param teacherId Filter by specific teacher (optional)
     * @param grade Filter by grade level (optional)
     * @param pageable  Pagination and sorting parameters (page, size, sort)
     * @return List of leaderboard entries with structured student/teacher details
     *
     * @example
     * GET /api/leaderboard?timeframe=MONTH&page=0&size=20&sort=points,desc
     * GET /api/leaderboard?page=1&size=10
     * GET /api/leaderboard?teacherId=123&grade=FIRST&page=1&size=10
     * GET /api/leaderboard?timeframe=WEEK&grade=SECOND&page=0&size-15
     */
    @GetMapping
    public Page<LeaderboardEntryDTO> getLeaderboard(
            @RequestParam(defaultValue = "WEEK") Timeframe timeframe,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String grade,
            @PageableDefault(size = 20) Pageable pageable) {
        return leaderboardService.getLeaderboard(timeframe, teacherId, grade, pageable);
    }
}
