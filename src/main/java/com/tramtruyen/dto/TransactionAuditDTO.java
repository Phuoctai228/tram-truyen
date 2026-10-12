package com.tramtruyen.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho một dòng giao dịch trên bảng đối soát (gộp đơn nạp và sổ cái ví).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAuditDTO {

    private String sourceType; // "TOPUP" hoặc "LEDGER"
    private Integer recordId;
    private String orderCode; // Mã đơn hàng (VD: TT123456) hoặc null/"—"
    private String displayCode; // "#TX-98421" hoặc orderCode
    private Integer userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private String type; // TOPUP, UNLOCK_CHAPTER, etc.
    private String typeDisplay; // "Nạp tiền VNPay", "Mở khóa chương", etc.
    private String typeBadgeClass;

    private String detail; // Tên gói Coin / Tên chương / Mô tả
    private Integer coinAmount; // Số Coin (+ hoặc -)
    private String coinAmountDisplay; // "+500 Coin" hoặc "-50 Coin"
    private String coinAmountClass; // text-emerald-600 hoặc text-rose-600

    private Integer amountVnd; // Số tiền VNĐ (nếu là đơn nạp)
    private String amountVndDisplay; // "+500.000 đ" hoặc "—"

    private String bankTransactionCode; // Mã GD ngân hàng hoặc "—"
    private String status; // PENDING, SUCCESS, FAILED, RECORDED
    private String statusDisplay; // Chờ thanh toán, Thành công, Thất bại, Đã ghi sổ
    private String statusBadgeClass;

    private LocalDateTime time;
    private String formattedTime;
    private boolean hasIpnLog; // true nếu là đơn nạp để hiện nút "Xem IPN Log"
}
