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
public class BanUserRequest {

    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId;

    @NotBlank(message = "Vui lòng chọn lý do xử lý")
    private String reason;

    @NotBlank(message = "Vui lòng chọn thời hạn đình chỉ")
    private String duration; // "3", "7", "30", "forever"

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
