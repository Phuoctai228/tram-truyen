package com.tramtruyen.service.impl;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.entity.CoinPackage;
import com.tramtruyen.repository.CoinPackageRepository;
import com.tramtruyen.service.CoinPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Cung cấp dữ liệu các gói nạp Coin.
 */
@Service
@RequiredArgsConstructor
public class CoinPackageServiceImpl implements CoinPackageService {

    private final CoinPackageRepository coinPackageRepository;

    @jakarta.annotation.PostConstruct
    @Transactional
    public void initPackages() {
        if (coinPackageRepository.count() == 0) {
            List<CoinPackage> initial = List.of(
                    CoinPackage.builder().name("Gói Khởi Điểm").price(20000).coinAmount(200).status("ACTIVE").build(),
                    CoinPackage.builder().name("Gói Tân Thủ (+10 Tặng)").price(50000).coinAmount(510).status("ACTIVE").build(),
                    CoinPackage.builder().name("Gói Tiêu Chuẩn (+50 Tặng)").price(100000).coinAmount(1050).status("ACTIVE").build(),
                    CoinPackage.builder().name("Gói Trưởng Thành (+150 Tặng)").price(200000).coinAmount(2150).status("ACTIVE").build(),
                    CoinPackage.builder().name("Gói Tiết Kiệm (+500 Tặng)").price(500000).coinAmount(5500).status("ACTIVE").build(),
                    CoinPackage.builder().name("Gói Đại Gia VIP (+1.200 Tặng)").price(1000000).coinAmount(11200).status("ACTIVE").build()
            );
            coinPackageRepository.saveAll(initial);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinPackageDTO> getActivePackages() {
        List<CoinPackage> packages = coinPackageRepository.findByStatusOrderByPriceAsc("ACTIVE");
        List<CoinPackageDTO> dtos = new ArrayList<>();

        for (CoinPackage pkg : packages) {
            // Tỷ lệ quy đổi chuẩn 1.000đ = 10 Coin (1đ = 0.01 Coin)
            int baseCoin = pkg.getPrice() / 100;
            int totalCoin = pkg.getCoinAmount();
            int bonusCoin = Math.max(0, totalCoin - baseCoin);

            String badge = null;
            if (pkg.getPrice() == 50000) {
                badge = "+10 Tặng";
            } else if (pkg.getPrice() == 100000) {
                badge = "+50 Tặng";
            } else if (pkg.getPrice() == 200000) {
                badge = "+150 Tặng";
            } else if (pkg.getPrice() == 500000) {
                badge = "Tiết kiệm 10%";
            } else if (pkg.getPrice() == 1000000) {
                badge = "Đại Gia VIP";
            }

            dtos.add(CoinPackageDTO.builder()
                    .id(pkg.getId())
                    .name(pkg.getName())
                    .price(pkg.getPrice())
                    .coinAmount(totalCoin)
                    .baseCoin(baseCoin)
                    .bonusCoin(bonusCoin)
                    .badgeText(badge)
                    .build());
        }
        return dtos;
    }
}
