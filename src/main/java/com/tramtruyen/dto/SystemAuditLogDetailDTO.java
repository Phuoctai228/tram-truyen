package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết nhật ký hệ thống cho modal.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLogDetailDTO {

    private Integer id;
    private Integer adminId;
    private String adminName;
    private String adminEmail;
    private String actionType;
    private String actionTypeDisplay;
    private String actionChipClass;

    private String targetEntity;
    private Integer targetId;
    private String targetDisplay;

    private String ipAddress;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;

    private String rawOldValue;
    private String prettyOldValue;
    private String rawNewValue;
    private String prettyNewValue;

    private boolean isJsonDiffAvailable;
    private List<AuditLogFieldDiffDTO> fieldDiffs;
}
