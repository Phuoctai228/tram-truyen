package com.tramtruyen.service.impl;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.dto.CoinPackageRequestDTO;
import com.tramtruyen.entity.CoinPackage;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.CoinPackageRepository;
import com.tramtruyen.service.CoinPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cung cấp dịch vụ quản lý và truy xuất dữ liệu các gói nạp Coin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoinPackageServiceImpl implements CoinPackageService {

    public static final int MAX_ACTIVE_PACKAGES = 6;
    public static final int MIN_PACKAGE_PRICE = 10000;
    public static final int MAX_PACKAGE_PRICE = 2000000;
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private final CoinPackageRepository coinPackageRepository;

    @jakarta.annotation.PostConstruct
    @Transactional
    public void initPackages() {
        if (coinPackageRepository.count() == 0) {
            List<CoinPackage> initial = List.of(
                    CoinPackage.builder().name("Gói Khởi Điểm").price(20000).coinAmount(200).status(STATUS_ACTIVE).build(),
                    CoinPackage.builder().name("Gói Tân Thủ (+10 Tặng)").price(50000).coinAmount(510).status(STATUS_ACTIVE).build(),
                    CoinPackage.builder().name("Gói Tiêu Chuẩn (+50 Tặng)").price(100000).coinAmount(1050).status(STATUS_ACTIVE).build(),
                    CoinPackage.builder().name("Gói Trưởng Thành (+150 Tặng)").price(200000).coinAmount(2150).status(STATUS_ACTIVE).build(),
                    CoinPackage.builder().name("Gói Tiết Kiệm (+500 Tặng)").price(500000).coinAmount(5500).status(STATUS_ACTIVE).build(),
                    CoinPackage.builder().name("Gói Đại Gia VIP (+1.200 Tặng)").price(1000000).coinAmount(11200).status(STATUS_ACTIVE).build()
            );
            coinPackageRepository.saveAll(initial);
            log.info("Khởi tạo thành công 6 gói nạp Coin mặc định.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinPackageDTO> getActivePackages() {
        List<CoinPackage> packages = coinPackageRepository.findByStatusOrderByPriceAsc(STATUS_ACTIVE);
        List<CoinPackageDTO> dtos = new ArrayList<>();

        for (CoinPackage pkg : packages) {
            int totalCoin = pkg.getCoinAmount();

            dtos.add(CoinPackageDTO.builder()
                    .id(pkg.getId())
                    .name(pkg.getName())
                    .price(pkg.getPrice())
                    .coinAmount(totalCoin)
                    .baseCoin(totalCoin)
                    .bonusCoin(0)
                    .badgeText(null)
                    .status(pkg.getStatus())
                    .build());
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinPackage> getAllPackages() {
        return coinPackageRepository.findAllByOrderByPriceAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public CoinPackage getPackageById(Integer id) {
        return coinPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói nạp Coin với ID: " + id));
    }

    @Override
    @Transactional
    public CoinPackage createPackage(CoinPackageRequestDTO dto) {
        validatePackageInput(dto);

        String trimmedName = dto.getName().trim();
        if (coinPackageRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Tên gói nạp '" + trimmedName + "' đã tồn tại trong hệ thống.");
        }

        String targetStatus = normalizeStatus(dto.getStatus());
        if (STATUS_ACTIVE.equals(targetStatus)) {
            checkActivePackageLimit(null);
        }

        CoinPackage coinPackage = CoinPackage.builder()
                .name(trimmedName)
                .price(dto.getPrice())
                .coinAmount(dto.getCoinAmount())
                .status(targetStatus)
                .build();

        CoinPackage saved = coinPackageRepository.save(coinPackage);
        log.info("Tạo mới thành công gói nạp Coin ID: {}, Tên: '{}', Giá: {}đ, Coin: {}, Trạng thái: {}",
                saved.getId(), saved.getName(), saved.getPrice(), saved.getCoinAmount(), saved.getStatus());
        return saved;
    }

    @Override
    @Transactional
    public CoinPackage updatePackage(Integer id, CoinPackageRequestDTO dto) {
        validatePackageInput(dto);
        CoinPackage pkg = getPackageById(id);

        String trimmedName = dto.getName().trim();
        if (coinPackageRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new IllegalArgumentException("Tên gói nạp '" + trimmedName + "' đã tồn tại trong hệ thống.");
        }

        String targetStatus = normalizeStatus(dto.getStatus());
        if (!STATUS_ACTIVE.equals(pkg.getStatus()) && STATUS_ACTIVE.equals(targetStatus)) {
            checkActivePackageLimit(id);
        }

        pkg.setName(trimmedName);
        pkg.setPrice(dto.getPrice());
        pkg.setCoinAmount(dto.getCoinAmount());
        pkg.setStatus(targetStatus);

        CoinPackage updated = coinPackageRepository.save(pkg);
        log.info("Cập nhật thành công gói nạp Coin ID: {}, Tên: '{}', Giá: {}đ, Coin: {}, Trạng thái: {}",
                updated.getId(), updated.getName(), updated.getPrice(), updated.getCoinAmount(), updated.getStatus());
        return updated;
    }

    @Override
    @Transactional
    public void togglePackageStatus(Integer id) {
        CoinPackage pkg = getPackageById(id);

        if (STATUS_ACTIVE.equalsIgnoreCase(pkg.getStatus())) {
            pkg.setStatus(STATUS_INACTIVE);
        } else {
            checkActivePackageLimit(id);
            pkg.setStatus(STATUS_ACTIVE);
        }

        coinPackageRepository.save(pkg);
        log.info("Chuyển đổi trạng thái gói nạp Coin ID: {} sang '{}'", id, pkg.getStatus());
    }

    @Override
    @Transactional
    public void deletePackage(Integer id) {
        CoinPackage pkg = getPackageById(id);
        coinPackageRepository.delete(pkg);
        log.info("Đã xóa gói nạp Coin ID: {}, Tên: '{}'", id, pkg.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinPackageDTO> suggestForShortfall(int shortfall, int limit) {
        List<CoinPackageDTO> all = getActivePackages();
        if (all.isEmpty()) {
            return List.of();
        }

        int maxResults = Math.max(1, limit);
        int bestIdx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getCoinAmount() >= shortfall) {
                bestIdx = i;
                break;
            }
        }

        // Nếu số coin thiếu vượt quá tất cả gói, chọn gói cao nhất
        if (bestIdx == -1) {
            bestIdx = all.size() - 1;
        }

        List<CoinPackageDTO> results = new ArrayList<>();
        int start = bestIdx;
        if (start + maxResults > all.size()) {
            start = Math.max(0, all.size() - maxResults);
        }

        for (int i = start; i < Math.min(all.size(), start + maxResults); i++) {
            CoinPackageDTO item = all.get(i);
            boolean isBest = (i == bestIdx);
            results.add(CoinPackageDTO.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .coinAmount(item.getCoinAmount())
                    .baseCoin(item.getBaseCoin())
                    .bonusCoin(0)
                    .badgeText(isBest ? "PHÙ HỢP" : null)
                    .status(item.getStatus())
                    .bestFit(isBest)
                    .build());
        }
        return results;
    }

    /**
     * Kiểm tra số lượng gói nạp ACTIVE không được vượt quá 6 gói.
     */
    private void checkActivePackageLimit(Integer currentEditingId) {
        long activeCount = coinPackageRepository.countByStatus(STATUS_ACTIVE);
        if (activeCount >= MAX_ACTIVE_PACKAGES) {
            throw new IllegalStateException("Hệ thống chỉ cho phép kích hoạt tối đa " + MAX_ACTIVE_PACKAGES
                    + " gói nạp (ACTIVE) để hiển thị cho thành viên. Vui lòng ẩn bớt một gói trước khi kích hoạt gói này.");
        }
    }

    private void validatePackageInput(CoinPackageRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu gói nạp không được để trống.");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên gói nạp không được để trống.");
        }
        if (dto.getPrice() == null) {
            throw new IllegalArgumentException("Giá gói nạp không được để trống.");
        }
        if (dto.getPrice() < MIN_PACKAGE_PRICE || dto.getPrice() > MAX_PACKAGE_PRICE) {
            throw new IllegalArgumentException("Giá gói nạp phải từ 10.000 VNĐ đến 2.000.000 VNĐ.");
        }
        if (dto.getPrice() % 1000 != 0) {
            throw new IllegalArgumentException("Giá gói nạp phải là số tròn nghìn (bội số của 1.000 VNĐ), không được nhập số lẻ như 335.350 VNĐ.");
        }
        // Tự động tính số Coin nhận theo tỷ lệ chuẩn: 1.000 VNĐ = 10 Coin (100 VNĐ = 1 Coin)
        dto.setCoinAmount(dto.getPrice() / 100);
    }

    private String normalizeStatus(String status) {
        if (STATUS_ACTIVE.equalsIgnoreCase(status)) {
            return STATUS_ACTIVE;
        }
        return STATUS_INACTIVE;
    }
}
