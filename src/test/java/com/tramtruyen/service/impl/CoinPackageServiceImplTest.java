package com.tramtruyen.service.impl;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.entity.CoinPackage;
import com.tramtruyen.repository.CoinPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
