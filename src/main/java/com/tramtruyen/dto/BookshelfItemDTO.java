package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object representing a novel in a user's bookshelf.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookshelfItemDTO {

    private Integer novelId;
    private String slug;
    private String title;
    private String author;
    private String coverUrl;
    private String status;
    private List<String> categories;
    private int totalChapters;
    private int currentChapterNumber;
    private int progressPercentage;
    private boolean isCompleted;
    private boolean hasVipChapters;
    private LocalDateTime addedAt;
    private String relativeTime;
    private String readUrl;
}
