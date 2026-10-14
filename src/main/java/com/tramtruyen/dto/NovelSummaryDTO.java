package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelSummaryDTO {
    private Integer id;
    private String title;
    private String slug;
    private String author;
    private String summary;
    private String coverUrl;
    private String status;
    private Integer views;
    private Long favoriteCount;
    private BigDecimal averageRating;
    private Integer chapterCount;
    private java.util.List<com.tramtruyen.entity.Category> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.util.List<LatestChapterDTO> latestChapters;
}
