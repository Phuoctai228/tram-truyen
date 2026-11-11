package com.tramtruyen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận dữ liệu từ form Thêm / Sửa gói nạp Coin của Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinPackageRequestDTO {

    @NotBlank(message = "Tên gói nạp không được để trống")
    @Size(max = 100, message = "Tên gói nạp không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Giá gói nạp không được để trống")
    @Min(value = 10000, message = "Giá gói nạp tối thiểu là 10.000 VNĐ")
    @Max(value = 2000000, message = "Giá gói nạp tối đa là 2.000.000 VNĐ")
    private Integer price;

    private Integer coinAmount;

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Trạng thái chỉ có thể là ACTIVE hoặc INACTIVE")
    @Builder.Default
    private String status = "ACTIVE";
}
