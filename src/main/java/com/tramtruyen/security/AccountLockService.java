package com.tramtruyen.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.AccountLockedInfoDTO;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockService {

    private static final Map<String, String> BAN_REASON_MAP = Map.of(
            "spam", "Vi phạm quy chuẩn cộng đồng: Phát tán spam quảng cáo hoặc bình luận rác trái phép.",
            "hate", "Vi phạm quy chuẩn cộng đồng: Phát tán bình luận đả kích cá nhân và nội dung tiêu cực nhiều lần.",
            "fraud", "Vi phạm chính sách: Gian lận hoặc trục lợi tính năng hệ thống.",
            "other", "Vi phạm điều khoản dịch vụ và quy chuẩn cộng đồng của Trạm Truyện."
    );

    private static final DateTimeFormatter BANNED_AT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm — dd/MM/yyyy");
    private static final DateTimeFormatter EXPIRES_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Checks if the user's ban has expired. If so, updates status to ACTIVE in-place,
     * logs AUTO_UNBAN audit entry, and returns true.
     */
    @Transactional
    public boolean checkAndAutoUnban(User user) {
        if (user == null || !"BANNED".equalsIgnoreCase(user.getStatus())) {
            return false;
        }

        Optional<SystemAuditLog> lastBanLog = systemAuditLogRepository
                .findFirstByTargetEntityAndTargetIdAndActionTypeOrderByCreatedAtDesc("USERS", user.getId(), "BAN_USER");

        if (lastBanLog.isEmpty()) {
            return false;
        }

        SystemAuditLog banLog = lastBanLog.get();
        if (banLog.getNewValue() == null || banLog.getNewValue().isBlank()) {
            return false;
        }

        try {
            Map<String, Object> data = objectMapper.readValue(banLog.getNewValue(), new TypeReference<>() {});
            String banExpiresAtStr = (String) data.get("banExpiresAt");

            // Permanent bans (banExpiresAt == null) never expire
            if (banExpiresAtStr == null || banExpiresAtStr.isBlank()) {
                return false;
            }

            LocalDateTime expiresAt = LocalDateTime.parse(banExpiresAtStr);
            if (LocalDateTime.now().isAfter(expiresAt)) {
                // Ban has expired! Perform auto-unban
                user.setStatus("ACTIVE");
                userRepository.save(user);

                // Write AUTO_UNBAN audit log using the original banLog's adminId
                Integer originalAdminId = banLog.getAdminId() != null ? banLog.getAdminId() : 1;
                Map<String, Object> unbanData = Map.of(
                        "status", "ACTIVE",
                        "auto", true,
                        "unbannedAt", LocalDateTime.now().toString(),
                        "originalBanExpiresAt", banExpiresAtStr
                );

                systemAuditLogRepository.save(SystemAuditLog.builder()
                        .adminId(originalAdminId)
                        .actionType("AUTO_UNBAN")
                        .targetEntity("USERS")
                        .targetId(user.getId())
                        .oldValue("{\"status\":\"BANNED\"}")
                        .newValue(objectMapper.writeValueAsString(unbanData))
                        .ipAddress("SYSTEM_SCHEDULER")
                        .build());

                log.info("Auto-unbanned expired account: user_id={}", user.getId());
                return true;
            }
        } catch (Exception e) {
            log.error("Error evaluating auto-unban for user_id=" + user.getId(), e);
        }

        return false;
    }

    /**
     * Builds the AccountLockedInfoDTO for displaying /account-locked page.
     * Guaranteed never to return null.
     */
    @Transactional(readOnly = true)
    public AccountLockedInfoDTO buildAccountLockedInfo(String email) {
        String supportEmail = getSupportEmail();

        if (email == null || email.isBlank()) {
            return buildGenericLockedInfo(supportEmail);
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return buildGenericLockedInfo(supportEmail);
        }

        User user = userOpt.get();
        Optional<SystemAuditLog> lastBanLog = systemAuditLogRepository
                .findFirstByTargetEntityAndTargetIdAndActionTypeOrderByCreatedAtDesc("USERS", user.getId(), "BAN_USER");

        if (lastBanLog.isEmpty()) {
            return buildGenericLockedInfo(supportEmail);
        }

        SystemAuditLog banLog = lastBanLog.get();
        if (banLog.getNewValue() == null || banLog.getNewValue().isBlank()) {
            return buildGenericLockedInfo(supportEmail);
        }

        try {
            Map<String, Object> data = objectMapper.readValue(banLog.getNewValue(), new TypeReference<>() {});

            String reasonKey = String.valueOf(data.getOrDefault("reason", ""));
            String reasonDisplay = BAN_REASON_MAP.getOrDefault(reasonKey, "Vi phạm quy chuẩn và điều khoản dịch vụ của Trạm Truyện.");

            LocalDateTime bannedAtTime = null;
            if (banLog.getCreatedAt() != null) {
                bannedAtTime = banLog.getCreatedAt();
            } else if (data.containsKey("bannedAt")) {
                try {
                    bannedAtTime = LocalDateTime.parse(String.valueOf(data.get("bannedAt")));
                } catch (Exception ignored) {}
            }
            if (bannedAtTime == null) {
                bannedAtTime = LocalDateTime.now();
            }
            String bannedAtFormatted = bannedAtTime.format(BANNED_AT_FORMATTER);

            String banExpiresAtStr = (String) data.get("banExpiresAt");
            String duration = String.valueOf(data.getOrDefault("duration", ""));

            boolean isPermanent = (banExpiresAtStr == null || banExpiresAtStr.isBlank() || "forever".equalsIgnoreCase(duration));
            String durationText;
            String subtitle;

            if (isPermanent) {
                durationText = "Khóa vĩnh viễn";
                subtitle = "Tài khoản của bạn đã bị khóa do vi phạm Điều khoản dịch vụ của Trạm Truyện.";
            } else {
                LocalDateTime expiresAt = LocalDateTime.parse(banExpiresAtStr);
                durationText = "Tạm khóa " + duration + " ngày (đến hết " + expiresAt.format(EXPIRES_DATE_FORMATTER) + ")";
                subtitle = "Tài khoản của bạn tạm thời bị khóa do phát hiện hành vi vi phạm Điều khoản dịch vụ của Trạm Truyện.";
            }

            String ticket = "#BAN-" + String.format("%05d", banLog.getId() != null ? banLog.getId() : user.getId());

            return AccountLockedInfoDTO.builder()
                    .title("Tài khoản đã bị khóa")
                    .subtitle(subtitle)
                    .reasonDisplay(reasonDisplay)
                    .bannedAtFormatted(bannedAtFormatted)
                    .durationFormatted(durationText)
                    .reviewer("Ban Kiểm duyệt Trạm Truyện")
                    .ticketNumber(ticket)
                    .supportEmail(supportEmail)
                    .permanent(isPermanent)
                    .generic(false)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing BAN_USER log for user_id=" + user.getId(), e);
            return buildGenericLockedInfo(supportEmail);
        }
    }

    private AccountLockedInfoDTO buildGenericLockedInfo(String supportEmail) {
        return AccountLockedInfoDTO.builder()
                .title("Tài khoản đã bị khóa")
                .subtitle("Tài khoản của bạn tạm thời bị khóa do phát hiện hành vi vi phạm Điều khoản dịch vụ của Trạm Truyện.")
                .reasonDisplay("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ ban quản trị để được hỗ trợ.")
                .bannedAtFormatted("Đang cập nhật")
                .durationFormatted("Theo quyết định của BQT")
                .reviewer("Ban Kiểm duyệt Trạm Truyện")
                .ticketNumber("#BAN-00000")
                .supportEmail(supportEmail)
                .permanent(false)
                .generic(true)
                .build();
    }

    private String getSupportEmail() {
        try {
            String found = jdbcTemplate.query(
                    "SELECT setting_value FROM system_settings WHERE setting_key = 'support_email'",
                    rs -> rs.next() ? rs.getString("setting_value") : null
            );
            if (found != null && !found.isBlank()) {
                return found.trim();
            }
        } catch (Exception e) {
            log.debug("Could not query support_email from system_settings, using fallback", e);
        }
        return "hotro@tramtruyen.vn";
    }
}
