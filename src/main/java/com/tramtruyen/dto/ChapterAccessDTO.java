package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Access resolution information and display content for a chapter view (UC-26).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterAccessDTO {

    private ChapterAccessStatus status;
    private boolean readable;
    private boolean paid;
    private int chapterPrice;
    private int walletBalance;
    private int remainingBalanceAfter;
    private int shortfall;
    private boolean canAfford;
    private boolean loggedIn;
    private List<String> paragraphs;
    private List<CoinPackageDTO> suggestedPackages;
    private String watermarkText;
    private String userFingerprint;
}
