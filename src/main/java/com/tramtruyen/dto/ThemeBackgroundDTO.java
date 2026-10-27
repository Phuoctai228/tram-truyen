package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho cấu hình ảnh nền mờ của Web Theme (UC-52).
 * Lưu trong system_settings dưới dạng JSON: {"url":"...","opacity":12,"blur":4}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeBackgroundDTO {
    private String url;
    private Integer opacity;
    private Integer blur;
}
