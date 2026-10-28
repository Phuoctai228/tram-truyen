package com.tramtruyen.dto;

import lombok.*;

/**
 * DTO hiển thị thông tin gói nạp Coin ra giao diện.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinPackageDTO {

    private Integer id;
    private String name;
    private Integer price;
    private Integer coinAmount;
    private Integer baseCoin;
    private Integer bonusCoin;
    private String badgeText;
    @Builder.Default
    private boolean bestFit = false;
}
