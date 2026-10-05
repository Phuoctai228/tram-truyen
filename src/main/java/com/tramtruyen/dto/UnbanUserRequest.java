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
public class UnbanUserRequest {

    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId;

    @NotBlank(message = "Vui lòng nhập lý do mở khóa")
    @Size(min = 20, max = 500, message = "Lý do mở khóa phải từ 20 đến 500 ký tự")
    private String unbanReason;

    @Builder.Default
    private Boolean sendNotification = true;
}
