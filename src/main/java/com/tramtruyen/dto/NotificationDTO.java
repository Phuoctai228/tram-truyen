package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer id;
    private Integer userId;
    private String recipientName;
    private String recipientEmail;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String timeFormatted;
    private String category; // SYSTEM, REPORT_FEEDBACK, REWARD, WARNING, etc.
    private String categoryDisplayName;
    private String referenceCode; // e.g. #RP-8841, #FB-1042
    private String statusBadge; // e.g. "Đã xử lý", "Hệ thống", "Đang rà soát"
    private boolean isSystem;
    private String sentTimeFormatted; // e.g. "24/05/2026 10:30"

    public boolean isSystem() {
        return this.isSystem;
    }
}
