package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO hiển thị danh sách nhật ký hệ thống trên bảng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLogDTO {

    private Integer id;
    private Integer adminId;
    private String adminName;
    private String adminEmail;
    private String adminAvatar;
    private String adminRoleBadge;

    private String actionType;
    private String actionTypeDisplay; // Nhãn tiếng Việt
    private String actionChipClass; // CSS tông màu của chip hành động

    private String targetEntity;
    private Integer targetId;
    private String targetDisplay;

    private String ipAddress;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
}
