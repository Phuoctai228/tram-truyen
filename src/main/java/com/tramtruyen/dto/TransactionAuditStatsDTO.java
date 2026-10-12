package com.tramtruyen.dto;

import lombok.*;

/**
 * Thống kê tổng quan đầu trang đối soát giao dịch (tính toàn hệ thống).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAuditStatsDTO {

    private Long totalTopupVnd; // Tổng tiền đã nạp (VNĐ)
    private Long totalCoinIssued; // Tổng Coin đã phát hành
    private Long totalCoinSpent; // Tổng Coin đã tiêu (giá trị tuyệt đối các biến động âm)
    private Long totalTransactionsCount; // Tổng số bản ghi (toàn hệ thống)
    private Long successTopupOrdersCount; // Số đơn nạp thành công
    private String topupGrowthText; // Ví dụ: "+14.8%"
    private String spendRatioText; // Tỷ lệ nạp / tiêu thụ
    private Double spendRatioPercent;
}
