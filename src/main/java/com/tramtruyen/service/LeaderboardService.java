package com.tramtruyen.service;

import com.tramtruyen.dto.LeaderboardDTO;

/**
 * Service for computing and delivering novel leaderboard data.
 */
public interface LeaderboardService {

    LeaderboardDTO getLeaderboard(Integer categoryId);

    LeaderboardDTO getLeaderboard(Integer categoryId, String timeframe);

    java.util.List<com.tramtruyen.dto.LeaderboardItemDTO> getLeaderboardItems(String timeframe, int limit);
}
