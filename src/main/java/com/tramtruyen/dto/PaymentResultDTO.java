package com.tramtruyen.dto;

import lombok.*;

/**
 * DTO chứa kết quả giao dịch VNPay trả về hiển thị trên giao diện kết quả.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {

    private boolean success;
    private String orderCode;
    private Integer amountVnd;
    private String paymentMethod;
    private Integer coinReceived;
    private String formattedTime;
    private Integer currentBalance;
    private String statusText;
    private String failureReason;
    private String responseCode;
}
