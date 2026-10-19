package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết nhật ký hệ thống cho trang server-rendered GET /admin/audit-logs/{id}.
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
    private String adminRoleBadge; // ROLE_ADMIN = "Quản trị viên", ROLE_STAFF = "Nhân viên", AUTO_UNBAN = null
    private String actionType;
    private String actionTypeDisplay;
    private String actionChipClass;

    private String targetEntity;
    private Integer targetId;
    private String targetDisplay;
    private String targetUrl; // Route thật tới đối tượng (nếu có)

    private String ipAddress;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;

    private boolean isJsonDiffAvailable;
    private List<AuditLogFieldDiffDTO> fieldDiffs;
    private String plainFallbackNote; // Nội dung ghi nhận nếu không phải JSON object hoặc rỗng
}
