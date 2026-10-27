package com.tramtruyen.controller;

import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.dto.PaymentResultDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.CoinPackageService;
import com.tramtruyen.service.SystemSettingService;
import com.tramtruyen.service.VNPayService;
import com.tramtruyen.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý luồng nạp Coin ví ảo và tích hợp cổng thanh toán VNPay Sandbox.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final CoinPackageService coinPackageService;
    private final VNPayService vnPayService;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;

    /**
     * Hiển thị giao diện Chọn gói nạp Coin vào tài khoản.
     */
    /**
     * Hiển thị giao diện Chọn gói nạp Coin vào tài khoản.
     */
    @GetMapping("/wallet/topup")
    public String showTopupPage(Authentication authentication,
                                @RequestParam(required = false) String returnUrl,
                                HttpServletRequest request,
                                Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            String redirectParam = returnUrl != null && !returnUrl.isBlank() ? "?redirect=" + returnUrl : "?redirect=/wallet/topup";
            return "redirect:/login" + redirectParam;
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            request.getSession().setAttribute("returnUrl", returnUrl);
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        List<CoinPackageDTO> packages = coinPackageService.getActivePackages();

        model.addAttribute("user", user);
        model.addAttribute("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0);
        model.addAttribute("packages", packages);
        model.addAttribute("vndToCoinRate", systemSettingService.getVndToCoinRate());
        model.addAttribute("vndPerCoinFormatted", systemSettingService.getVndPerCoinFormatted());
        model.addAttribute("returnUrl", returnUrl);

        return "wallet/topup";
    }

    /**
     * Tiếp nhận yêu cầu chọn gói và chuyển hướng người dùng sang VNPay Sandbox.
     */
    @PostMapping("/wallet/topup/process")
    public String processTopup(Authentication authentication,
                               @RequestParam("packageId") Integer packageId,
                               @RequestParam(required = false) String returnUrl,
                               HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/wallet/topup";
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            request.getSession().setAttribute("returnUrl", returnUrl);
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        String clientIp = VNPayUtil.getIpAddress(request);
        String vnpayUrl = vnPayService.createPaymentUrl(user, packageId, clientIp);

        return "redirect:" + vnpayUrl;
    }

    /**
     * Tiếp nhận chuyển hướng từ VNPay về sau khi người dùng thực hiện thanh toán trên cổng.
     */
    @GetMapping("/payment/vnpay-return")
    public String handlePaymentReturn(@RequestParam Map<String, String> allParams,
                                      HttpServletRequest request,
                                      Model model) {
        PaymentResultDTO result = vnPayService.processReturn(allParams);
        model.addAttribute("result", result);

        String returnUrl = (String) request.getSession().getAttribute("returnUrl");
        if (returnUrl != null && !returnUrl.isBlank()) {
            model.addAttribute("returnUrl", returnUrl);
            request.getSession().removeAttribute("returnUrl");
        }

        if (result.isSuccess()) {
            return "wallet/payment-success";
        } else {
            return "wallet/payment-failure";
        }
    }

    /**
     * Endpoint Webhook / IPN dành cho máy chủ VNPay gọi ngầm để xác thực giao dịch an toàn.
     */
    @GetMapping("/api/payment/vnpay-ipn")
    @ResponseBody
    public Map<String, String> handlePaymentIpn(@RequestParam Map<String, String> allParams,
                                                HttpServletRequest request) {
        String clientIp = VNPayUtil.getIpAddress(request);
        log.info("Nhận tín hiệu IPN từ VNPay (IP: {}): {}", clientIp, allParams);
        return vnPayService.processIpn(allParams, clientIp);
    }
}
