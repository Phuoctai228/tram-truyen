package com.tramtruyen.service;

import com.tramtruyen.dto.DailyCheckinResultDTO;
import com.tramtruyen.dto.DailyCheckinStatusDTO;

/**
 * Service interface for daily attendance check-in gamification and streak rewards.
 */
public interface DailyCheckinService {

    /**
     * Retrieves the current daily check-in status, streak count, and 7-day roadmap.
     *
     * @param email user email
     * @return DailyCheckinStatusDTO for dashboard or modal display
     */
    DailyCheckinStatusDTO getCheckinStatus(String email);

    /**
     * Executes the daily check-in transaction, granting free coin rewards and advancing streaks.
     *
     * @param email user email
     * @return DailyCheckinResultDTO with result, coins won, and new balance
     */
    DailyCheckinResultDTO performCheckin(String email);
}
