package com.tramtruyen.service;

import com.tramtruyen.dto.PaymentResultDTO;
import com.tramtruyen.entity.User;

import java.util.Map;

/**
 * Service xử lý tích hợp cổng thanh toán VNPay Sandbox.
 */
public interface VNPayService {

    /**
     * Khởi tạo đơn nạp tiền PENDING và tạo URL thanh toán VNPay.
     *
     * @param user      Người dùng thực hiện thanh toán
     * @param packageId ID gói nạp Coin
     * @param ipAddress IP của người dùng
     * @return URL thanh toán chuyển hướng sang VNPay
     */
    String createPaymentUrl(User user, Integer packageId, String ipAddress);

    /**
     * Xử lý dữ liệu trả về trên trình duyệt khi người dùng hoàn tất thanh toán VNPay.
     *
     * @param params Các tham số query từ VNPay
     * @return Kết quả thanh toán PaymentResultDTO
     */
    PaymentResultDTO processReturn(Map<String, String> params);

    /**
     * Xử lý thông báo Instant Payment Notification (IPN) gọi ngầm từ máy chủ VNPay.
     *
     * @param params    Các tham số từ IPN request
     * @param ipAddress IP máy chủ gọi đến
     * @return Map phản hồi chứa RspCode và Message theo chuẩn VNPay
     */
    Map<String, String> processIpn(Map<String, String> params, String ipAddress);
}
