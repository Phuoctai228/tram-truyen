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
}
