package com.tramtruyen.service.impl;

import com.tramtruyen.dto.TransactionDTO;
import com.tramtruyen.dto.WalletOverviewDTO;
import com.tramtruyen.entity.TopupOrder;
import com.tramtruyen.entity.Transaction;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.enums.TopupOrderStatus;
import com.tramtruyen.entity.enums.TransactionType;
import com.tramtruyen.exception.InsufficientBalanceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.TopupOrderRepository;
import com.tramtruyen.repository.TransactionRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.tramtruyen.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Triển khai các nghiệp vụ ví cá nhân, quản lý số dư và truy vết biến động số dư.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TopupOrderRepository topupOrderRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public Integer getBalance(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getWalletBalance() != null ? u.getWalletBalance() : 0)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    @Override
    @Transactional
    public void deductCoins(User user, int amount, String type, Integer referenceId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số Coin cần trừ phải lớn hơn 0");
        }

        // Lock user record with PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) to prevent race conditions
        User lockedUser = userRepository.findByIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        int currentBalance = lockedUser.getWalletBalance() != null ? lockedUser.getWalletBalance() : 0;
        if (currentBalance < amount) {
            throw new InsufficientBalanceException(amount, currentBalance);
        }

        int newBalance = currentBalance - amount;
        lockedUser.setWalletBalance(newBalance);
        userRepository.save(lockedUser);
        syncSecurityContextBalance(lockedUser, newBalance);

        // Record in ledger
        Transaction tx = Transaction.builder()
                .user(lockedUser)
                .amount(-amount)
                .balanceAfter(newBalance)
                .type(type)
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(tx);

        log.info("Deducted {} coins from user {}. New balance: {}", amount, lockedUser.getEmail(), newBalance);
    }

    @Override
    @Transactional
    public void addCoins(User user, int amount, String type, Integer referenceId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số Coin cộng phải lớn hơn 0");
        }

        User lockedUser = userRepository.findByIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        int currentBalance = lockedUser.getWalletBalance() != null ? lockedUser.getWalletBalance() : 0;
        int newBalance = currentBalance + amount;
        lockedUser.setWalletBalance(newBalance);
        userRepository.save(lockedUser);
        syncSecurityContextBalance(lockedUser, newBalance);

        Transaction tx = Transaction.builder()
                .user(lockedUser)
                .amount(amount)
                .balanceAfter(newBalance)
                .type(type)
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(tx);

        log.info("Credited {} coins to user {}. New balance: {}", amount, lockedUser.getEmail(), newBalance);
    }

    private void syncSecurityContextBalance(User user, int newBalance) {
        if (user == null || user.getId() == null) {
            return;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                if (cud.getUser() != null && user.getId().equals(cud.getUser().getId())) {
                    cud.getUser().setWalletBalance(newBalance);
                }
            }
        } catch (Exception e) {
            log.warn("Không thể đồng bộ số dư vào SecurityContext: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(User user, int amount) {
        if (user == null) {
            return false;
        }
        int balance;
        if (user.getId() != null) {
            balance = userRepository.findById(user.getId())
                    .map(u -> u.getWalletBalance() != null ? u.getWalletBalance() : 0)
                    .orElseGet(() -> user.getWalletBalance() != null ? user.getWalletBalance() : 0);
        } else {
            balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        }
        return balance >= amount;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletOverviewDTO getWalletOverview(User user) {
        User freshUser = userRepository.findById(user.getId()).orElse(user);
        int balance = freshUser.getWalletBalance() != null ? freshUser.getWalletBalance() : 0;
        String formattedBalance = formatNumber(balance);

        String walletCode = String.format("#WALLET-%06d", freshUser.getId());
        String statusText = "ACTIVE".equalsIgnoreCase(freshUser.getStatus()) ? "Sẵn sàng sử dụng" : "Đang tạm khóa";

        // Tìm thời gian giao dịch hoặc đơn nạp gần nhất
        LocalDateTime latestTime = null;
        var latestTxOpt = transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(freshUser.getId());
        if (latestTxOpt.isPresent()) {
            latestTime = latestTxOpt.get().getCreatedAt();
        }

        var failedOrders = topupOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(freshUser.getId(), TopupOrderStatus.FAILED.name());
        if (!failedOrders.isEmpty()) {
            LocalDateTime failedTime = failedOrders.get(0).getCreatedAt();
            if (latestTime == null || (failedTime != null && failedTime.isAfter(latestTime))) {
                latestTime = failedTime;
            }
        }

        String lastUpdated = "Chưa có biến động";
        if (latestTime != null) {
            if (latestTime.toLocalDate().isEqual(LocalDate.now())) {
                lastUpdated = latestTime.format(TIME_ONLY_FORMATTER) + " hôm nay";
            } else {
                lastUpdated = latestTime.format(DATE_TIME_FORMATTER);
            }
        }

        long totalFailed = failedOrders.size();
        long totalTx = transactionRepository.countByUserId(freshUser.getId()) + totalFailed;
        Page<TransactionDTO> initialPage = searchTransactions(freshUser, null, null, 0, 5);

        return WalletOverviewDTO.builder()
                .userId(freshUser.getId())
                .userFullName(freshUser.getFullName())
                .userEmail(freshUser.getEmail())
                .userAvatarUrl(freshUser.getAvatarUrl())
                .walletBalance(balance)
                .formattedBalance(formattedBalance)
                .walletCode(walletCode)
                .statusText(statusText)
                .lastUpdated(lastUpdated)
                .totalTransactions(totalTx)
                .transactions(initialPage.getContent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> searchTransactions(User user, String type, String keyword, LocalDate date, int page, int size) {
        String cleanType = (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim()))
                ? type.trim().toUpperCase()
                : null;

        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty())
                ? keyword.trim()
                : null;

        List<TransactionDTO> allList = new ArrayList<>();

        // 1. Lấy toàn bộ biến động số dư từ sổ cái transactions
        List<Transaction> txList = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Transaction t : txList) {
            if (cleanType != null && !cleanType.equalsIgnoreCase(t.getType())) {
                continue;
            }
            if (date != null && (t.getCreatedAt() == null || !t.getCreatedAt().toLocalDate().isEqual(date))) {
                continue;
            }
            TransactionDTO dto = mapToDTO(t);
            if (matchesKeyword(dto, cleanKeyword)) {
                allList.add(dto);
            }
        }

        // 2. Lấy các đơn nạp tiền bị hủy / thất bại từ topup_orders (khi xem Tất cả hoặc Nạp Coin)
        if (cleanType == null || "DEPOSIT".equalsIgnoreCase(cleanType)) {
            List<TopupOrder> failedOrders = topupOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), TopupOrderStatus.FAILED.name());
            for (TopupOrder order : failedOrders) {
                if (date != null && (order.getCreatedAt() == null || !order.getCreatedAt().toLocalDate().isEqual(date))) {
                    continue;
                }
                TransactionDTO dto = mapFailedOrderToDTO(order);
                if (matchesKeyword(dto, cleanKeyword)) {
                    allList.add(dto);
                }
            }
        }

        // 3. Sắp xếp toàn bộ giao dịch theo thời gian tạo mới nhất
        allList.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // 4. Phân trang
        Pageable pageable = PageRequest.of(page, size);
        int total = allList.size();
        int fromIndex = Math.min((int) pageable.getOffset(), total);
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);
        List<TransactionDTO> pageContent = allList.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, pageable, total);
    }

    private boolean matchesKeyword(TransactionDTO dto, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        String kw = keyword.toLowerCase();
        String cleanKw = kw.startsWith("#") ? kw.substring(1) : kw;
        if (cleanKw.startsWith("tx")) cleanKw = cleanKw.substring(2);

        boolean matchTitle = dto.getTitle() != null && dto.getTitle().toLowerCase().contains(kw);
        boolean matchSub = dto.getSubtitle() != null && dto.getSubtitle().toLowerCase().contains(cleanKw);
        boolean matchOrderCode = dto.getOrderCode() != null && dto.getOrderCode().toLowerCase().contains(cleanKw);
        boolean matchAmount = dto.getAmountDisplay() != null && dto.getAmountDisplay().toLowerCase().contains(kw);
        boolean matchSecondary = dto.getSecondaryText() != null && dto.getSecondaryText().toLowerCase().contains(kw);

        return matchTitle || matchSub || matchOrderCode || matchAmount || matchSecondary;
    }

    private TransactionDTO mapToDTO(Transaction t) {
        boolean isDeposit = TransactionType.DEPOSIT.name().equalsIgnoreCase(t.getType());
        boolean isReward = "GIFT_COIN".equalsIgnoreCase(t.getType()) || "DAILY_CHECKIN".equalsIgnoreCase(t.getType());
        boolean isPositive = t.getAmount() != null && t.getAmount() > 0;
        String formattedDate = t.getCreatedAt() != null ? t.getCreatedAt().format(DATE_TIME_FORMATTER) : "";

        String title;
        String subtitle;
        String amountDisplay;
        String secondaryText;
        String statusBadge;
        String icon;
        String iconBgColor;
        String amountColorClass;

        if (isDeposit) {
            title = (t.getDescription() != null && !t.getDescription().isEmpty())
                    ? t.getDescription()
                    : "Nạp Coin qua VNPay";
            subtitle = "Mã GD: #TX" + t.getId() + " • " + formattedDate;
            amountDisplay = "+ " + formatNumber(t.getAmount()) + " Coin";
            amountColorClass = "text-[#2B231D] font-bold";

            // Tìm thông tin tiền VNĐ tương ứng
            int amountVnd = t.getAmount() * 100;
            if (t.getReferenceId() != null) {
                TopupOrder order = topupOrderRepository.findById(t.getReferenceId()).orElse(null);
                if (order != null && order.getAmountVnd() != null) {
                    amountVnd = order.getAmountVnd();
                }
            }
            secondaryText = formatNumber(amountVnd) + "đ";
            statusBadge = "Thành công";
            icon = "south_west";
            iconBgColor = "bg-[#FEF3C7] text-[#D97706]";
        } else if (isReward || isPositive) {
            title = (t.getDescription() != null && !t.getDescription().isEmpty())
                    ? t.getDescription()
                    : (isReward ? "Thưởng điểm danh hàng ngày" : "Cộng Coin vào ví");
            subtitle = (isReward ? "Thưởng sự kiện • " : "Biến động số dư • ") + formattedDate;
            amountDisplay = "+ " + formatNumber(t.getAmount()) + " Coin";
            amountColorClass = "text-[#15803D] font-bold";
            secondaryText = isReward ? "Miễn phí" : "Hệ thống";
            statusBadge = "Thành công";
            icon = isReward ? "featured_seasonal_and_gifts" : "add_circle";
            iconBgColor = "bg-[#ECFDF5] text-[#15803D]";
        } else {
            title = (t.getDescription() != null && !t.getDescription().isEmpty())
                    ? t.getDescription()
                    : "Mở khóa chương VIP";
            subtitle = "Đọc truyện bản quyền • " + formattedDate;
            int cost = Math.abs(t.getAmount() != null ? t.getAmount() : 0);
            amountDisplay = "- " + formatNumber(cost) + " Coin";
            amountColorClass = "text-[#B91C1C] font-bold";
            secondaryText = "Vĩnh viễn";
            statusBadge = "Hoàn tất";
            icon = "lock";
            iconBgColor = "bg-[#FEE2E2] text-[#DC2626]";
        }

        return TransactionDTO.builder()
                .id(t.getId())
                .orderCode("#TX" + t.getId())
                .type(t.getType())
                .title(title)
                .subtitle(subtitle)
                .amount(t.getAmount())
                .amountDisplay(amountDisplay)
                .secondaryText(secondaryText)
                .statusBadge(statusBadge)
                .icon(icon)
                .iconBgColor(iconBgColor)
                .amountColorClass(amountColorClass)
                .createdAt(t.getCreatedAt())
                .formattedDate(formattedDate)
                .build();
    }

    private TransactionDTO mapFailedOrderToDTO(TopupOrder order) {
        String formattedDate = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
        int coin = order.getCoinReceived() != null ? order.getCoinReceived() : 0;
        int vnd = order.getAmountVnd() != null ? order.getAmountVnd() : 0;

        return TransactionDTO.builder()
                .id(order.getId())
                .orderCode("#" + order.getOrderCode())
                .type(TransactionType.DEPOSIT.name())
                .title("Nạp " + formatNumber(coin) + " Coin qua VNPay (Đơn hàng " + order.getOrderCode() + ")")
                .subtitle("Mã GD: #" + order.getOrderCode() + " • " + formattedDate)
                .amount(0)
                .amountDisplay("+ " + formatNumber(coin) + " Coin")
                .secondaryText(formatNumber(vnd) + "đ (Đã hủy)")
                .statusBadge("Đã hủy")
                .icon("cancel")
                .iconBgColor("bg-[#FEE2E2] text-[#DC2626]")
                .amountColorClass("text-[#9CA3AF] line-through font-bold")
                .createdAt(order.getCreatedAt())
                .formattedDate(formattedDate)
                .build();
    }

    private String formatNumber(int number) {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY); // định dạng phân cách hàng nghìn bằng dấu chấm (1.250)
        return nf.format(number);
    }
}
