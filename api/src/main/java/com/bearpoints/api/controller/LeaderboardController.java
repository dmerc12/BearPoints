package com.bearpoints.api.controller;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for leaderboard data retrieval.
 * <p>Provides secure access to student point leaderboards filtered by timeframe.
 *
 * <p>Endpoint:
 * <ul>
 *     <li>{@code GET /api/leaderboard} - Retrieves current leaderboard</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 *     <li>Requires STUDENT, TEACHER, or ADMIN role</li>
 *     <li>Supports timeframe filtering (WEEK, MONTH, SEMESTER, YEAR)</li>
 *     <li>Default timeframe is WEEK</li>
 *     <li>Returns sorted list (highest points first)</li>
 * </ul>
 *
 * @see LeaderboardService
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @version 1.0
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
     * Retrieves current leaderboard data.
     * <p>Returns list of students sorted by points in descending order.
     *
     * @param timeframe Filter period (optional, defaults to WEEK)
     * @return List of leaderboard entries with structured student/teacher details
     */
    @GetMapping
    public List<LeaderboardEntryDTO> getLeaderboard(
            @RequestParam(defaultValue = "WEEK") Timeframe timeframe) {
        return leaderboardService.getLeaderboard(timeframe);
    }
}
