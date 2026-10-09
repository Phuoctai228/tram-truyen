package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a chapter summary displayed on novel cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestChapterDTO {

    private Integer id;
    private Integer chapterNumber;
    private String title;
    private LocalDateTime updatedAt;
    private String relativeTime;
}
