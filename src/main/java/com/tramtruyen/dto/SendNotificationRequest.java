package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotBlank(message = "Vui lòng chọn phạm vi người nhận")
    private String scope; // ALL, SPECIFIC

    private Integer recipientId;

    private String recipientName;

    private String recipientEmail;

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Size(max = 255, message = "Tiêu đề thông báo không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    @Size(max = 2000, message = "Nội dung thông báo không được vượt quá 2000 ký tự")
    private String content;
}
