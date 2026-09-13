package com.tramtruyen.dto;

import com.tramtruyen.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageDTO {
    private List<Category> categories;
    private List<NovelSummaryDTO> recentlyUpdatedNovels;
    private List<NovelSummaryDTO> hotNovels;
    private List<NovelSummaryDTO> completedNovels;
}
