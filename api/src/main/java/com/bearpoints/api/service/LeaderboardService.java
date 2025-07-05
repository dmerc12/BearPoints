package com.bearpoints.api.service;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;

import java.util.List;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implemented with {@link LeaderboardServiceImpl}
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface LeaderboardService {
    /** Service to assist in retrieving and calculating the brag log leaderboard */
    List<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe);
}
