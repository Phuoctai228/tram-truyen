package com.tramtruyen.dto;

import lombok.*;

/**
 * Thống kê 3 thẻ chỉ số đầu trang Nhật ký hệ thống (toàn bộ bảng, không bị ảnh hưởng bởi bộ lọc).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLogStatsDTO {

    private Long totalRecords; // Tổng số bản ghi
    private Long todayRecords; // Số bản ghi hôm nay (từ 00:00:00)
    private Long activeAdminsLast24h; // Số quản trị viên đã thao tác trong 24 giờ qua (distinct admin_id)
    private String todayGrowthText;

    public Long getTotalLogs() {
        return totalRecords;
    }

    public Long getTodayLogs() {
        return todayRecords;
    }

    public static class SystemAuditLogStatsDTOBuilder {
        public SystemAuditLogStatsDTOBuilder totalLogs(Long totalLogs) {
            this.totalRecords = totalLogs;
            return this;
        }

        public SystemAuditLogStatsDTOBuilder todayLogs(Long todayLogs) {
            this.todayRecords = todayLogs;
            return this;
        }
    }
}
