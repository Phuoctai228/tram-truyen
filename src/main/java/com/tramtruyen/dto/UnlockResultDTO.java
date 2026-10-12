package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result DTO returned after unlocking a paid VIP chapter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockResultDTO {

    private boolean success;
    private Integer chapterId;
    private Integer chapterNumber;
    private String chapterTitle;
    private Integer pricePaid;
    private Integer remainingBalance;
    private Boolean autoUnlockNext;
    private String message;
}
