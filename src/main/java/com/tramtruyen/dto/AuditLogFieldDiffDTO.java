package com.tramtruyen.dto;

import lombok.*;

/**
 * DTO chứa thông tin khác biệt của từng trường thay đổi (Tên trường | Nhãn tiếng Việt | Giá trị cũ | Giá trị mới | Loại đổi).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFieldDiffDTO {

    private String fieldName;
    private String fieldLabel;
    private String oldValue;
    private String newValue;
    private String diffType; // ADDED, MODIFIED, DELETED
    private String diffTypeLabel; // Thêm mới, Đã sửa, Đã xóa
    private boolean longText; // Có phải văn bản dài cần line-clamp và details/summary
}
