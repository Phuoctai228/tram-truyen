package com.tramtruyen.service;

import com.tramtruyen.dto.TransactionDTO;
import com.tramtruyen.dto.WalletOverviewDTO;
import com.tramtruyen.entity.User;
import org.springframework.data.domain.Page;

/**
 * Service quản lý thông tin ví và lịch sử biến động số dư.
 */
public interface WalletService {

    /**
     * Lấy dữ liệu tổng quan ví và các giao dịch gần đây của người dùng.
     */
    WalletOverviewDTO getWalletOverview(User user);

    /**
     * Tìm kiếm và lọc danh sách biến động số dư có phân trang.
     */
    Page<TransactionDTO> searchTransactions(User user, String type, String keyword, int page, int size);
}
