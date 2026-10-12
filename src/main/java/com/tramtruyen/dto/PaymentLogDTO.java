package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về cho modal xem chi tiết IPN log của một đơn nạp.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLogDTO {

    private Integer id;
    private String orderCode;
    private String rawPayload;
    private String prettyPayload;
    private String ipAddress;
    private String status; // SUCCESS, UNMATCHED, ERROR
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
}
