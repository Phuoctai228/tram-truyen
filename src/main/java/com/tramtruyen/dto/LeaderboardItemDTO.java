package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a novel entry on the Leaderboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardItemDTO {

    private Integer rank;
    private String formattedRank; // "01", "02", ..., "10"
    private Integer novelId;
    private String title;
    private String slug;
    private String author;
    private String summary;
    private String coverUrl;
    private String status;
    private Integer views;
    private String formattedViews;
    private BigDecimal averageRating;
    private Integer chapterCount;
    private String primaryCategory;
}
