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
    private String author;
    private String coverUrl;
    private String status;
    private Integer views;
    private BigDecimal averageRating;
    private LocalDateTime updatedAt;
}
