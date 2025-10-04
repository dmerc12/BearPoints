package com.bearpoints.api.dao;

import com.bearpoints.api.dao.impl.LeaderboardDAOImpl;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Data Access Object for complex leaderboard queries with server-side ranking.
 * <p>Handles database-level aggregation and dynamic ranking calculation for leaderboard entries,
 * providing optimal performance by leveraging SQL window functions.
 *
 * <p>Primary Functionality:
 * <ul>
 *     <li>Executes complex ranking queries with point aggregation</li>
 *     <li>Supports dynamic filtering by teacher and grade level</li>
 *     <li>Provides database-agnostic JPQL implementation</li>
 *     <li>Maintains correct ranking context withing filtered results</li>
 * </ul>
 *
 * @see LeaderboardEntryDTO
 * @see LeaderboardDAOImpl
 * @version 1.0
 * @author Dylan Mercer
 */
public interface LeaderboardDAO {
    /**
     * Finds ranked leaderboard entries with server-side ranking calculation.
     * <p>Uses RANK() window function to calculate student positions based on total points.
     * Ranking is dynamically recalculated for filtered result sets.
     *
     * @param startDate Filter logs after this date (inclusive)
     * @param teacherId Filter by specific teacher (optional, null for all teachers)
     * @param grade Filter by grade level (optional, null for all grades)
     * @param pageable Pagination and sorting configuration
     * @return Page of ranked leaderboard entries with contextual ranking
     *      (global rank for overall, class rank when filtered by teacher, etc.)
     *
     * @example
     * - Overall leaderboard - ranks 1,2,3... for all students
     * findRankedLeaderboard(startDate, null, null, pageable)
     * - Class leaderboard - ranks 1,2,3... within specific class
     * findRankedLeaderboard(startDate, 123L, null, pageable)
     * - Grade leaderboard - ranks 1,2,3... within grade level
     * findRankedLeaderboard(startDate, null, "FIRST", pageable)
     */
    Page<LeaderboardEntryDTO> findRankedLeaderboard(
            LocalDateTime startDate,
            Long teacherId,
            String grade,
            Pageable pageable
    );
}
