package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing an item in the reader's reading history list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistoryItemDTO {

    private Integer activityId;
    private Integer novelId;
    private String novelSlug;
    private String novelTitle;
    private String novelCoverUrl;
    private String author;
    private List<String> categories;
    private Integer chapterId;
    private Integer chapterNumber;
    private String chapterTitle;
    private Boolean isVip;
    private Integer price;
    private LocalDateTime readAt;
    private String relativeTime;
    private String readUrl;
}
