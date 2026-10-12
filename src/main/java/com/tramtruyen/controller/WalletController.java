package com.tramtruyen.controller;

import com.tramtruyen.dto.TransactionDTO;
import com.tramtruyen.dto.WalletOverviewDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.SystemSettingService;
import com.tramtruyen.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller phục vụ trang xem Ví của tôi và API tìm kiếm lịch sử giao dịch.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;

    /**
     * Hiển thị trang Ví của tôi & Lịch sử giao dịch.
     */
    @GetMapping("/wallet")
    public String showWallet(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/wallet";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        WalletOverviewDTO overview = walletService.getWalletOverview(user);
        model.addAttribute("wallet", overview);
        model.addAttribute("user", user);
        model.addAttribute("vndToCoinRate", systemSettingService.getVndToCoinRate());
        model.addAttribute("vndPerCoinFormatted", systemSettingService.getVndPerCoinFormatted());

        return "wallet/index";
    }

    /**
     * API tra cứu, lọc và phân trang lịch sử giao dịch cho client.
     */
    @GetMapping("/api/wallet/transactions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTransactionsApi(
            Authentication authentication,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        Page<TransactionDTO> txPage = walletService.searchTransactions(user, type, keyword, page, size);

        return ResponseEntity.ok(Map.of(
                "content", txPage.getContent(),
                "currentPage", txPage.getNumber(),
                "totalPages", txPage.getTotalPages(),
                "totalElements", txPage.getTotalElements(),
                "isLast", txPage.isLast()
        ));
    }
}
