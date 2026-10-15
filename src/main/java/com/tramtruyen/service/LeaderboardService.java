package com.tramtruyen.service;

import com.tramtruyen.dto.LeaderboardDTO;

/**
 * Service for computing and delivering novel leaderboard data.
 */
public interface LeaderboardService {

    LeaderboardDTO getLeaderboard(Integer categoryId);
}
