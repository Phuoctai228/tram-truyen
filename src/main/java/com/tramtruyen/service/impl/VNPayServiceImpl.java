package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.config.VNPayProperties;
import com.tramtruyen.dto.PaymentResultDTO;
import com.tramtruyen.entity.*;
import com.tramtruyen.entity.enums.TopupOrderStatus;
import com.tramtruyen.entity.enums.TransactionType;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.*;
import com.tramtruyen.service.VNPayService;
import com.tramtruyen.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Hiện thực nghiệp vụ cổng thanh toán VNPay Sandbox.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayProperties vnPayProperties;
    private final CoinPackageRepository coinPackageRepository;
    private final TopupOrderRepository topupOrderRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter VNPAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm — dd/MM/yyyy");

    @Override
    @Transactional
    public String createPaymentUrl(User user, Integer packageId, String ipAddress) {
        CoinPackage coinPackage = coinPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói nạp Coin không tồn tại"));

        // Sinh mã đơn hàng duy nhất: TT + Timestamp + Random 3 chữ số
        String orderCode = "TT" + System.currentTimeMillis() + (int) (Math.random() * 900 + 100);

        // Khởi tạo đơn nạp trạng thái PENDING
        TopupOrder order = TopupOrder.builder()
                .user(user)
                .coinPackage(coinPackage)
                .orderCode(orderCode)
                .amountVnd(coinPackage.getPrice())
                .coinReceived(coinPackage.getCoinAmount())
                .status(TopupOrderStatus.PENDING.name())
                .paymentMethod("VNPAY")
                .build();
        topupOrderRepository.save(order);

        // Chuẩn bị các tham số theo tài liệu chuẩn của VNPay v2.1.0
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime expire = now.plusMinutes(15);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayProperties.getVersion());
        vnpParams.put("vnp_Command", vnPayProperties.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        // Số tiền theo luật VNPay phải nhân 100
        vnpParams.put("vnp_Amount", String.valueOf((long) coinPackage.getPrice() * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode);
        vnpParams.put("vnp_OrderInfo", "Nap " + coinPackage.getCoinAmount() + " Coin vao Vi - " + orderCode);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", (ipAddress != null && !ipAddress.isEmpty()) ? ipAddress : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMATTER));
        vnpParams.put("vnp_ExpireDate", expire.format(VNPAY_DATE_FORMATTER));

        // Băm chữ ký bảo mật HMAC-SHA512
        Map<String, String> buildResult = VNPayUtil.buildQueryAndHashData(vnpParams);
        String secureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), buildResult.get("hashData"));
        String paymentUrl = vnPayProperties.getPayUrl() + "?" + buildResult.get("queryString") + "&vnp_SecureHash=" + secureHash;

        log.info("Khởi tạo đơn nạp {} thành công, chuyển hướng VNPay URL: {}", orderCode, paymentUrl);
        return paymentUrl;
    }

    @Override
    @Transactional
    public PaymentResultDTO processReturn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        // Sao chép map để loại bỏ hash trước khi tính toán kiểm tra chữ ký
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        Map<String, String> buildResult = VNPayUtil.buildQueryAndHashData(verifyParams);
        String expectedHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), buildResult.get("hashData"));

        boolean isChecksumValid = expectedHash.equalsIgnoreCase(receivedHash);
        boolean isSuccess = isChecksumValid && "00".equals(responseCode);

        TopupOrder order = topupOrderRepository.findByOrderCode(orderCode).orElse(null);

        // Xử lý hoàn tất đơn hàng ngay tại return nếu chữ ký hợp lệ (hỗ trợ môi trường localhost và dự phòng IPN)
        if (isSuccess && order != null && TopupOrderStatus.PENDING.name().equals(order.getStatus())) {
            fulfillOrder(order, transactionNo);
        } else if (!isSuccess && order != null && TopupOrderStatus.PENDING.name().equals(order.getStatus())) {
            order.setStatus(TopupOrderStatus.FAILED.name());
            topupOrderRepository.save(order);
        }

        Integer amountVnd = order != null ? order.getAmountVnd() : 0;
        Integer coinReceived = order != null ? order.getCoinReceived() : 0;
        String formattedTime = order != null && order.getCreatedAt() != null
                ? order.getCreatedAt().format(DISPLAY_DATE_FORMATTER)
                : LocalDateTime.now().format(DISPLAY_DATE_FORMATTER);

        Integer currentBalance = 0;
        if (order != null && order.getUser() != null) {
            User user = userRepository.findById(order.getUser().getId()).orElse(order.getUser());
            currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        }

        String statusText;
        String failureReason = null;

        if (isSuccess) {
            statusText = "Thành công";
        } else {
            statusText = "Hết hạn / Chưa trừ tiền";
            if (!isChecksumValid) {
                failureReason = "Chữ ký dữ liệu từ cổng thanh toán không hợp lệ (Bảo mật).";
            } else {
                failureReason = getFriendlyErrorMessage(responseCode);
            }
        }

        return PaymentResultDTO.builder()
                .success(isSuccess)
                .orderCode(orderCode != null ? "#" + orderCode : "N/A")
                .amountVnd(amountVnd)
                .paymentMethod("VNPay QR")
                .coinReceived(coinReceived)
                .formattedTime(formattedTime)
                .currentBalance(currentBalance)
                .statusText(statusText)
                .failureReason(failureReason)
                .responseCode(responseCode)
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> processIpn(Map<String, String> params, String ipAddress) {
        Map<String, String> response = new HashMap<>();

        // 1. Lưu vết raw payload ngay lập tức vào bảng payment_logs
        String rawJson = "";
        try {
            rawJson = objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            rawJson = params.toString();
        }

        PaymentLog paymentLog = PaymentLog.builder()
                .orderCode(params.get("vnp_TxnRef"))
                .rawPayload(rawJson)
                .ipAddress(ipAddress)
                .status("PROCESSING")
                .build();
        paymentLogRepository.save(paymentLog);

        // 2. Xác thực chữ ký
        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        Map<String, String> buildResult = VNPayUtil.buildQueryAndHashData(verifyParams);
        String expectedHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), buildResult.get("hashData"));

        if (!expectedHash.equalsIgnoreCase(receivedHash)) {
            paymentLog.setStatus("INVALID_CHECKSUM");
            paymentLogRepository.save(paymentLog);
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return response;
        }

        // 3. Kiểm tra đơn hàng trong cơ sở dữ liệu
        String orderCode = params.get("vnp_TxnRef");
        TopupOrder order = topupOrderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            paymentLog.setStatus("UNMATCHED_ORDER");
            paymentLogRepository.save(paymentLog);
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }

        // 4. Kiểm tra số tiền
        long vnpAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100;
        if (vnpAmount != (long) order.getAmountVnd()) {
            paymentLog.setStatus("INVALID_AMOUNT");
            paymentLogRepository.save(paymentLog);
            response.put("RspCode", "04");
            response.put("Message", "Invalid Amount");
            return response;
        }

        // 5. Kiểm tra trạng thái đơn hàng (chỉ xử lý nếu đang PENDING)
        if (!TopupOrderStatus.PENDING.name().equals(order.getStatus())) {
            paymentLog.setStatus("ALREADY_PROCESSED");
            paymentLogRepository.save(paymentLog);
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        // 6. Xử lý cập nhật số dư ví và lưu sổ cái giao dịch
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            fulfillOrder(order, transactionNo);
            paymentLog.setStatus("SUCCESS");
            paymentLogRepository.save(paymentLog);
        } else {
            order.setStatus(TopupOrderStatus.FAILED.name());
            topupOrderRepository.save(order);
            paymentLog.setStatus("PAYMENT_FAILED");
            paymentLogRepository.save(paymentLog);
            log.warn("Đơn nạp {} thanh toán thất bại tại VNPay, responseCode={}", orderCode, responseCode);
        }

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }

    /**
     * Đồng bộ cập nhật hoàn tất đơn hàng, cộng tiền ví và tạo giao dịch sổ cái.
     */
    private synchronized void fulfillOrder(TopupOrder order, String transactionNo) {
        if (!TopupOrderStatus.PENDING.name().equals(order.getStatus())) {
            log.info("Đơn hàng {} đã được xử lý trước đó (trạng thái: {})", order.getOrderCode(), order.getStatus());
            return;
        }

        order.setStatus(TopupOrderStatus.SUCCESS.name());
        order.setBankTransactionCode(transactionNo);
        order.setPaidAt(LocalDateTime.now());
        topupOrderRepository.save(order);

        // Cập nhật số dư User trong Database
        User user = userRepository.findById(order.getUser().getId()).orElse(order.getUser());
        int currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        int newBalance = currentBalance + order.getCoinReceived();
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        // Đồng bộ số dư vào SecurityContext session nếu user đang đăng nhập
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.tramtruyen.security.CustomUserDetails cud) {
                if (cud.getUser().getId().equals(user.getId())) {
                    cud.getUser().setWalletBalance(newBalance);
                }
            }
        } catch (Exception e) {
            log.warn("Không thể cập nhật session balance: {}", e.getMessage());
        }

        // Ghi nhật ký biến động số dư vào transactions
        Transaction tx = Transaction.builder()
                .user(user)
                .amount(order.getCoinReceived())
                .balanceAfter(newBalance)
                .type(TransactionType.DEPOSIT.name())
                .referenceId(order.getId())
                .description("Nạp " + order.getCoinReceived() + " Coin qua VNPay (Đơn hàng " + order.getOrderCode() + ")")
                .build();
        transactionRepository.save(tx);
        log.info("Cộng tiền thành công cho đơn {}: +{} Coin, số dư mới {}", order.getOrderCode(), order.getCoinReceived(), newBalance);
    }

    private String getFriendlyErrorMessage(String responseCode) {
        if (responseCode == null) {
            return "Giao dịch không thành công.";
        }
        return switch (responseCode) {
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ Internet Banking tại ngân hàng.";
            case "10" -> "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch.";
            case "12" -> "Thẻ/Tài khoản của bạn đã bị khóa.";
            case "13" -> "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24" -> "Khách hàng đã hủy giao dịch thanh toán.";
            case "51" -> "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default -> "Giao dịch bị gián đoạn hoặc thất bại từ cổng đối tác (Mã lỗi: " + responseCode + ").";
        };
    }
}
