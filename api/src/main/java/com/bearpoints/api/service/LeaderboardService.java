package com.bearpoints.api.service;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.Timeframe;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe);
}
