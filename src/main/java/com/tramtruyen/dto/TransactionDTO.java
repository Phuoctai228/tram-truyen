package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin từng biến động số dư trong lịch sử giao dịch ví.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Integer id;
    private String orderCode;
    private String type; // DEPOSIT, UNLOCK_CHAPTER
    private String title;
    private String subtitle;
    private Integer amount;
    private String amountDisplay; // + 500 Coin, - 15 Coin
    private String secondaryText; // 50.000đ hoặc Vĩnh viễn
    private String statusBadge; // Thành công / Hoàn tất
    private String icon; // Google Material Symbol icon name
    private String iconBgColor; // Tailwind background & text color
    private String amountColorClass; // Text color class
    private LocalDateTime createdAt;
    private String formattedDate;
}
