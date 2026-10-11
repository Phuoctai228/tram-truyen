package com.tramtruyen.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {

    @NotNull(message = "Tỷ giá quy đổi VNĐ sang Coin không được để trống")
    @Min(value = 1, message = "Tỷ giá phải là số nguyên dương lớn hơn 0")
    @Max(value = 1000000, message = "Tỷ giá không vượt quá 1.000.000")
    private Integer vndToCoinRate;

    @NotBlank(message = "Hotline hỗ trợ không được để trống")
    @Pattern(regexp = "^[0-9+\\-\\.\\s]{8,20}$", message = "Hotline chỉ gồm số, dấu +, khoảng trắng, dấu gạch nối, độ dài từ 8 đến 20 ký tự")
    private String contactPhone;

    @NotBlank(message = "Email hỗ trợ không được để trống")
    @Email(message = "Email hỗ trợ không đúng định dạng")
    @Size(max = 100, message = "Email không vượt quá 100 ký tự")
    private String supportEmail;

    @NotBlank(message = "Điều khoản sử dụng không được để trống")
    @Size(max = 20000, message = "Điều khoản sử dụng tối đa 20.000 ký tự")
    private String termsOfService;

    @NotBlank(message = "Chính sách bảo mật không được để trống")
    @Size(max = 20000, message = "Chính sách bảo mật tối đa 20.000 ký tự")
    private String privacyPolicy;

    private String lastUpdatedFormatted;
    private String lastUpdatedByAdmin;
}
