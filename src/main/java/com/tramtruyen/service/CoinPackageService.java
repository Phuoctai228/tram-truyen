package com.tramtruyen.service;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.dto.CoinPackageRequestDTO;
import com.tramtruyen.entity.CoinPackage;

import java.util.List;

/**
 * Service quản lý danh sách gói nạp Coin.
 */
public interface CoinPackageService {

    /**
     * Lấy danh sách các gói nạp Coin đang hoạt động (tối đa 6 gói) để hiển thị ra giao diện member.
     *
     * @return Danh sách CoinPackageDTO
     */
    List<CoinPackageDTO> getActivePackages();

    /**
     * Lấy danh sách toàn bộ gói nạp Coin trong hệ thống cho Admin quản lý.
     *
     * @return Danh sách CoinPackage sắp xếp theo giá tăng dần
     */
    List<CoinPackage> getAllPackages();

    /**
     * Lấy thông tin chi tiết một gói nạp theo ID.
     *
     * @param id ID của gói nạp
     * @return Entity CoinPackage
     */
    CoinPackage getPackageById(Integer id);

    /**
     * Tạo mới một gói nạp Coin (ràng buộc tối đa 6 gói ACTIVE).
     *
     * @param dto dữ liệu gói nạp cần tạo
     * @return Entity CoinPackage vừa tạo
     */
    CoinPackage createPackage(CoinPackageRequestDTO dto);

    /**
     * Cập nhật thông tin gói nạp Coin (ràng buộc tối đa 6 gói ACTIVE khi chuyển status).
     *
     * @param id ID của gói nạp
     * @param dto dữ liệu cập nhật
     * @return Entity CoinPackage sau khi cập nhật
     */
    CoinPackage updatePackage(Integer id, CoinPackageRequestDTO dto);

    /**
     * Xóa một gói nạp Coin khỏi hệ thống.
     *
     * @param id ID của gói nạp cần xóa
     */
    void deletePackage(Integer id);

    /**
     * Chuyển đổi trạng thái Ẩn/Hiện (ACTIVE ↔ INACTIVE) của gói nạp (ràng buộc tối đa 6 gói ACTIVE).
     *
     * @param id ID của gói nạp
     */
    void togglePackageStatus(Integer id);

    /**
     * Gợi ý các gói nạp phù hợp nhất khi độc giả bị thiếu Coin để mở chương VIP (UC-26).
     *
     * @param shortfall số Coin còn thiếu (> 0)
     * @param limit số gói tối đa cần lấy (thường là 2 gói như mockup)
     * @return danh sách gói nạp gợi ý được gắn cờ bestFit cho gói tối ưu
     */
    List<CoinPackageDTO> suggestForShortfall(int shortfall, int limit);
}
