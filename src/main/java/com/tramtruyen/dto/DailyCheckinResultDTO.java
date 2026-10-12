package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result DTO returned after performing a daily attendance check-in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheckinResultDTO {

    private boolean success;
    private Integer rewardCoin;
    private Integer streakCount;
    private Integer newBalance;
    private String message;
}
