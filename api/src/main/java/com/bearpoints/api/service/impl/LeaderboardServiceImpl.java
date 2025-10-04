package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.LeaderboardDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implements {@link LeaderboardService}
 *
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @see LeaderboardDAO
 *
 * @version 3.0
 * @author Dylan Mercer
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    private final LeaderboardDAO leaderboardDAO;

    public LeaderboardServiceImpl(LeaderboardDAO leaderboardDAO) {

        this.leaderboardDAO = leaderboardDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe, Long teacherId, String grade, Pageable pageable) {
        LocalDateTime startDate = calculateStartDate(timeframe);
        return leaderboardDAO.findRankedLeaderboard(startDate, teacherId, grade, pageable);
    }

    private LocalDateTime calculateStartDate(Timeframe timeframe) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return switch (timeframe) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case SEMESTER -> now.minusMonths(6);
            case YEAR -> now.minusYears(1);
        };
    }
}
