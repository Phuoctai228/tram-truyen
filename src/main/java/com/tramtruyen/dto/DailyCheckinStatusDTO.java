package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO providing comprehensive daily check-in status and 7-day roadmap state for members.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheckinStatusDTO {

    private String serverDate;
    private Integer weekNumber;
    private Integer year;
    private Long totalCheckinDays;
    private Integer streakCount;
    private Integer currentStreakDay; // 1 to 7
    private Integer todayReward;
    private Boolean isCheckedInToday;
    private Integer walletBalance;
    private Integer totalRewardCoins;
    private List<DayRoadmapItemDTO> roadmap;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayRoadmapItemDTO {
        private Integer dayNumber; // 1..7
        private Integer rewardCoin; // 5, 5, 10, 10, 15, 15, 30
        private String status; // "CLAIMED", "TODAY_PENDING", "TODAY_CLAIMED", "LOCKED"
        private String label; // "Đã nhận", "Chờ nhận", "Hôm nay", "Ngày mai", "Hôm qua", etc.
        private Boolean isSpecial; // true for day 7 milestone
    }
}
