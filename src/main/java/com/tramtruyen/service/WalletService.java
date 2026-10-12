package com.tramtruyen.service;

import com.tramtruyen.dto.TransactionDTO;
import com.tramtruyen.dto.WalletOverviewDTO;
import com.tramtruyen.entity.User;
import org.springframework.data.domain.Page;

/**
 * Service quản lý thông tin ví, số dư và lịch sử giao dịch.
 */
public interface WalletService {

    /**
     * Gets current wallet coin balance of a user by email.
     *
     * @param email user email
     * @return current coin balance
     */
    Integer getBalance(String email);

    /**
     * Deducts coins from a user's wallet with concurrency safety and writes ledger transaction.
     *
     * @param user user entity
     * @param amount number of coins to deduct (positive integer)
     * @param type transaction type identifier (e.g. UNLOCK_CHAPTER)
     * @param referenceId associated entity ID (e.g. chapterId)
     * @param description transaction description note
     */
    void deductCoins(User user, int amount, String type, Integer referenceId, String description);

    /**
     * Adds coins to a user's wallet and writes ledger transaction.
     *
     * @param user user entity
     * @param amount number of coins to add (positive integer)
     * @param type transaction type identifier (e.g. DAILY_CHECKIN, TOPUP_COIN)
     * @param referenceId associated entity ID
     * @param description transaction description note
     */
    void addCoins(User user, int amount, String type, Integer referenceId, String description);

    /**
     * Checks if a user has sufficient coin balance for a specific amount.
     *
     * @param user user entity
     * @param amount required coin amount
     * @return true if balance is greater than or equal to amount
     */
    boolean hasSufficientBalance(User user, int amount);

    /**
     * Lấy dữ liệu tổng quan ví và các giao dịch gần đây của người dùng.
     *
     * @param user user entity
     * @return thông tin tổng quan ví
     */
    WalletOverviewDTO getWalletOverview(User user);

    /**
     * Tìm kiếm và lọc danh sách biến động số dư có phân trang.
     *
     * @param user user entity
     * @param type loại giao dịch
     * @param keyword từ khóa tìm kiếm
     * @param page số trang (0-indexed)
     * @param size kích thước trang
     * @return trang danh sách giao dịch DTO
     */
    Page<TransactionDTO> searchTransactions(User user, String type, String keyword, int page, int size);
}
