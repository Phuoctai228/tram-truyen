package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContentRequest {

    @NotNull(message = "Chương được báo cáo không hợp lệ")
    private Integer chapterId;

    @NotBlank(message = "Vui lòng chọn loại vấn đề")
    private String issueType;

    @NotBlank(message = "Chi tiết vấn đề không được để trống")
    @Size(min = 10, max = 500, message = "Chi tiết vấn đề phải từ 10 đến 500 ký tự")
    private String description;
}
