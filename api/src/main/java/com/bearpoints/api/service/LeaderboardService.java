package com.bearpoints.api.service;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implemented with {@link LeaderboardServiceImpl}
 *
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @version 2.0
 * @author Dylan Mercer
 */
public interface LeaderboardService {
    /**
     * Retrieves paginated leaderboard data for the specified timeframe
     * @param timeframe Filter period for leaderboard data
     * @param pageable Pagination information
     * @return Page of leaderboard entries with structured student/teacher details
     */
    Page<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe, Pageable pageable);
}
