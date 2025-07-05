package com.bearpoints.api.controller;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Represents route controller responsible for returning brag log leaderboard.
 *
 * @see LeaderboardService
 * @see LeaderboardEntryDTO
 * @see Timeframe
 *
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

    /** Internal brag log leaderboard retrieval route controller */
    @GetMapping
    public List<LeaderboardEntryDTO> getLeaderboard(
            @RequestParam(defaultValue = "WEEK") Timeframe timeframe) {
        return leaderboardService.getLeaderboard(timeframe);
    }
}
