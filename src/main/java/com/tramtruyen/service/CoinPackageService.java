package com.tramtruyen.service;

import com.tramtruyen.dto.CoinPackageDTO;

import java.util.List;

/**
 * Service quản lý danh sách gói nạp Coin.
 */
public interface CoinPackageService {

    /**
     * Lấy danh sách các gói nạp Coin đang hoạt động để hiển thị ra giao diện.
     *
     * @return Danh sách CoinPackageDTO
     */
    List<CoinPackageDTO> getActivePackages();

    /**
     * Gợi ý các gói nạp phù hợp nhất khi độc giả bị thiếu Coin để mở chương VIP (UC-26).
     *
     * @param shortfall số Coin còn thiếu (> 0)
     * @param limit số gói tối đa cần lấy (thường là 2 gói như mockup)
     * @return danh sách gói nạp gợi ý được gắn cờ bestFit cho gói tối ưu
     */
    List<CoinPackageDTO> suggestForShortfall(int shortfall, int limit);
}
