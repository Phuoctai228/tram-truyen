package com.tramtruyen.dto;

import com.tramtruyen.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data bundle for rendering the Leaderboard page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {

    private String currentTimeframe;
    private Integer currentCategoryId;
    private List<Category> categories;
    private LeaderboardItemDTO top1;
    private LeaderboardItemDTO top2;
    private LeaderboardItemDTO top3;
    private List<LeaderboardItemDTO> restItems;
    private int totalCount;
}
