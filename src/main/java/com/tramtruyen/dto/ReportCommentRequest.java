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
public class ReportCommentRequest {

    @NotNull(message = "Bình luận được báo cáo không hợp lệ")
    private Integer commentId;

    @NotBlank(message = "Vui lòng chọn loại vi phạm")
    private String violationType;

    @NotBlank(message = "Chi tiết lý do báo cáo không được để trống")
    @Size(max = 500, message = "Chi tiết lý do không được vượt quá 500 ký tự")
    private String description;
}
