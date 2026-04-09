package com.bearpoints.api.service;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.impl.LeaderboardServiceImpl;
import org.springframework.data.domain.Pageable;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implemented with {@link LeaderboardServiceImpl}
 *
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @version 3.2
 * @author Dylan Mercer
 */
public interface LeaderboardService {
    /**
     * Retrieves paginated data with dynamic server-side ranking.
     * <p>Enhanced in Version 3.0 with database-level ranking and filtering support.
     *
     * @param timeframe Filter period for leaderboard data
     * @param teacherId Filter by specific teacher (optional)
     * @param grade Filter by grade level (optional)
     * @param pageable Pagination information
     * @return Page of leaderboard entries with contextual ranking
     * @since 3.0 Added teacherId and grade filtering parameters
     */
    PagedResponseDTO<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe, Long teacherId, GradeLevel grade, Pageable pageable);
}
