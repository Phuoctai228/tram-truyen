package com.tramtruyen.entity.enums;

/**
 * Trạng thái quy trình kiểm duyệt báo cáo vi phạm.
 */
public enum ReportStatus {
    RECEIVED("Chờ xử lý"),
    PROCESSED("Đã xử lý"),
    REJECTED("Từ chối");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
