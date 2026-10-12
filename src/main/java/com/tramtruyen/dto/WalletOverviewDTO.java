package com.tramtruyen.dto;

import lombok.*;

import java.util.List;

/**
 * DTO tổng quan ví cá nhân và danh sách lịch sử giao dịch.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletOverviewDTO {
    private Integer userId;
    private String userFullName;
    private String userEmail;
    private String userAvatarUrl;
    private Integer walletBalance;
    private String formattedBalance; // e.g. "1.250"
    private String walletCode; // e.g. "#WALLET-883921"
    private String statusText; // e.g. "Sẵn sàng sử dụng"
    private String lastUpdated; // e.g. "14:30 hôm nay"
    private long totalTransactions;
    private List<TransactionDTO> transactions;
}
