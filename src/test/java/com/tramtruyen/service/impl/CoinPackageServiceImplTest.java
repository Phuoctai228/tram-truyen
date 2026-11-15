package com.tramtruyen.service.impl;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.dto.CoinPackageRequestDTO;
import com.tramtruyen.entity.CoinPackage;
import com.tramtruyen.repository.CoinPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinPackageServiceImplTest {

    @Mock
    private CoinPackageRepository coinPackageRepository;

    private CoinPackageServiceImpl coinPackageService;

    @BeforeEach
    void setUp() {
        coinPackageService = new CoinPackageServiceImpl(coinPackageRepository);
    }

    @Test
    @DisplayName("getActivePackages trả về danh sách gói nạp active chuẩn xác")
    void getActivePackages_returnsActivePackagesAccurately() {
        List<CoinPackage> packages = List.of(
                CoinPackage.builder().id(1).name("Gói Khởi Điểm").price(20000).coinAmount(200).status("ACTIVE").build(),
                CoinPackage.builder().id(2).name("Gói Tân Thủ").price(50000).coinAmount(500).status("ACTIVE").build(),
                CoinPackage.builder().id(3).name("Gói Tiêu Chuẩn").price(100000).coinAmount(1000).status("ACTIVE").build()
        );
        when(coinPackageRepository.findByStatusOrderByPriceAsc("ACTIVE")).thenReturn(packages);

        List<CoinPackageDTO> dtos = coinPackageService.getActivePackages();

        assertThat(dtos).hasSize(3);

        assertThat(dtos.get(0).getName()).isEqualTo("Gói Khởi Điểm");
        assertThat(dtos.get(0).getPrice()).isEqualTo(20000);
        assertThat(dtos.get(0).getCoinAmount()).isEqualTo(200);

        assertThat(dtos.get(1).getName()).isEqualTo("Gói Tân Thủ");
        assertThat(dtos.get(1).getPrice()).isEqualTo(50000);
        assertThat(dtos.get(1).getCoinAmount()).isEqualTo(500);

        assertThat(dtos.get(2).getName()).isEqualTo("Gói Tiêu Chuẩn");
        assertThat(dtos.get(2).getPrice()).isEqualTo(100000);
        assertThat(dtos.get(2).getCoinAmount()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Tạo gói nạp mới tự động tính toán số Coin = price / 100 (1.000đ = 10 Coin)")
    void createPackage_autoCalculatesCoinFromPrice() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Đặc Biệt")
                .price(335000)
                .status("ACTIVE")
                .build();

        when(coinPackageRepository.existsByNameIgnoreCase("Gói Đặc Biệt")).thenReturn(false);
        when(coinPackageRepository.countByStatus("ACTIVE")).thenReturn(3L);
        when(coinPackageRepository.save(any(CoinPackage.class))).thenAnswer(inv -> inv.getArgument(0));

        CoinPackage created = coinPackageService.createPackage(request);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Gói Đặc Biệt");
        assertThat(created.getPrice()).isEqualTo(335000);
        // Tự động tính 335.000 VNĐ -> 3.350 Coin
        assertThat(created.getCoinAmount()).isEqualTo(3350);
        assertThat(created.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Chặn tạo gói nạp có giá dưới 10.000 VNĐ")
    void createPackage_throwsWhenPriceBelow10k() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Quá Rẻ")
                .price(9000)
                .status("ACTIVE")
                .build();

        assertThatThrownBy(() -> coinPackageService.createPackage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("từ 10.000 VNĐ đến 2.000.000 VNĐ");

        verify(coinPackageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn tạo gói nạp có giá trên 2.000.000 VNĐ")
    void createPackage_throwsWhenPriceAbove2M() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Quá Đắt")
                .price(2500000)
                .status("ACTIVE")
                .build();

        assertThatThrownBy(() -> coinPackageService.createPackage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("từ 10.000 VNĐ đến 2.000.000 VNĐ");

        verify(coinPackageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn tạo gói nạp có giá lẻ (không chia hết cho 1.000 VNĐ như 335.350 VNĐ)")
    void createPackage_throwsWhenPriceNotDivisibleBy1000() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Số Lẻ")
                .price(335350)
                .status("ACTIVE")
                .build();

        assertThatThrownBy(() -> coinPackageService.createPackage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("số tròn nghìn (bội số của 1.000 VNĐ)");

        verify(coinPackageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn tạo gói nạp ACTIVE khi đã có đủ 6 gói ACTIVE")
    void createPackage_throwsWhenExceedingActiveLimit() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Thứ Bảy")
                .price(30000)
                .status("ACTIVE")
                .build();

        when(coinPackageRepository.existsByNameIgnoreCase("Gói Thứ Bảy")).thenReturn(false);
        when(coinPackageRepository.countByStatus("ACTIVE")).thenReturn(6L);

        assertThatThrownBy(() -> coinPackageService.createPackage(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tối đa 6 gói nạp (ACTIVE)");

        verify(coinPackageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cho phép tạo gói nạp trạng thái INACTIVE ngay cả khi đã có 6 gói ACTIVE")
    void createPackage_allowsInactiveWhen6ActiveExist() {
        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Ẩn Dự Phòng")
                .price(40000)
                .status("INACTIVE")
                .build();

        when(coinPackageRepository.existsByNameIgnoreCase("Gói Ẩn Dự Phòng")).thenReturn(false);
        when(coinPackageRepository.save(any(CoinPackage.class))).thenAnswer(inv -> inv.getArgument(0));

        CoinPackage created = coinPackageService.createPackage(request);

        assertThat(created.getStatus()).isEqualTo("INACTIVE");
        assertThat(created.getCoinAmount()).isEqualTo(400);
        verify(coinPackageRepository, never()).countByStatus(anyString());
    }

    @Test
    @DisplayName("Chặn đổi trạng thái sang ACTIVE khi đã có đủ 6 gói ACTIVE")
    void updatePackage_throwsWhenActivatingExceedsLimit() {
        CoinPackage existing = CoinPackage.builder()
                .id(10)
                .name("Gói Cũ")
                .price(30000)
                .coinAmount(300)
                .status("INACTIVE")
                .build();

        CoinPackageRequestDTO request = CoinPackageRequestDTO.builder()
                .name("Gói Cũ Cập Nhật")
                .price(30000)
                .status("ACTIVE")
                .build();

        when(coinPackageRepository.findById(10)).thenReturn(Optional.of(existing));
        when(coinPackageRepository.existsByNameIgnoreCaseAndIdNot("Gói Cũ Cập Nhật", 10)).thenReturn(false);
        when(coinPackageRepository.countByStatus("ACTIVE")).thenReturn(6L);

        assertThatThrownBy(() -> coinPackageService.updatePackage(10, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tối đa 6 gói nạp (ACTIVE)");
    }

    @Test
    @DisplayName("Bật tắt trạng thái (toggleStatus) thành công và tuân thủ giới hạn 6 gói")
    void togglePackageStatus_success() {
        CoinPackage activePkg = CoinPackage.builder().id(1).status("ACTIVE").build();
        when(coinPackageRepository.findById(1)).thenReturn(Optional.of(activePkg));

        coinPackageService.togglePackageStatus(1);
        assertThat(activePkg.getStatus()).isEqualTo("INACTIVE");

        // Toggle lại từ INACTIVE sang ACTIVE khi count < 6
        when(coinPackageRepository.countByStatus("ACTIVE")).thenReturn(4L);
        coinPackageService.togglePackageStatus(1);
        assertThat(activePkg.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Xóa gói nạp thành công")
    void deletePackage_success() {
        CoinPackage pkg = CoinPackage.builder().id(5).name("Gói Xóa").build();
        when(coinPackageRepository.findById(5)).thenReturn(Optional.of(pkg));

        coinPackageService.deletePackage(5);

        verify(coinPackageRepository).delete(pkg);
    }

    @Test
    void suggestForShortfallPicksSmallestSufficientPackageAsBestFit() {
        List<CoinPackage> packages = List.of(
                CoinPackage.builder().id(1).price(20000).coinAmount(200).status("ACTIVE").build(),
                CoinPackage.builder().id(2).price(50000).coinAmount(510).status("ACTIVE").build(),
                CoinPackage.builder().id(3).price(100000).coinAmount(1050).status("ACTIVE").build()
        );
        when(coinPackageRepository.findByStatusOrderByPriceAsc("ACTIVE")).thenReturn(packages);

        List<CoinPackageDTO> suggestions = coinPackageService.suggestForShortfall(30, 2);

        assertThat(suggestions).hasSize(2);
        assertThat(suggestions.get(0).getId()).isEqualTo(1);
        assertThat(suggestions.get(0).isBestFit()).isTrue();
        assertThat(suggestions.get(0).getBadgeText()).isEqualTo("PHÙ HỢP");
        assertThat(suggestions.get(1).getId()).isEqualTo(2);
        assertThat(suggestions.get(1).isBestFit()).isFalse();
    }
}
