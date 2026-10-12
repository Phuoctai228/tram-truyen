package com.tramtruyen.dto;

import lombok.*;

/**
 * DTO chứa thông tin khác biệt của từng trường thay đổi (Khóa | Giá trị cũ | Giá trị mới).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFieldDiffDTO {

    private String fieldName;
    private String oldValue;
    private String newValue;
}
