package com.bearpoints.api.service;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;

import java.util.List;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implemented with {@link LeaderboardServiceImpl}
 *
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @version 1.0
 * @author Dylan Mercer
 */
public interface LeaderboardService {
    /**
     * Retrieves leaderboard data for the specified timeframe
     * @param timeframe Filter period for leaderboard data
     * @return List of leaderboard entries with structured student/teacher details
     */
    List<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe);
}
