package com.tramtruyen.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.*;
import com.tramtruyen.entity.Notification;
import com.tramtruyen.entity.Role;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.NotificationRepository;
import com.tramtruyen.repository.RoleRepository;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private static final Set<String> VALID_ROLES = Set.of("ROLE_MEMBER", "ROLE_STAFF", "ROLE_ADMIN");
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "BANNED", "PENDING_VERIFICATION");
    private static final Set<String> VALID_BAN_REASONS = Set.of("spam", "hate", "fraud", "other");
    private static final Set<String> VALID_BAN_DURATIONS = Set.of("3", "7", "30", "forever");

    private static final Map<String, String> BAN_REASON_MAP = Map.of(
            "spam", "Spam quảng cáo thương mại / bình luận rác trái phép",
            "hate", "Xúc phạm thành viên, ngôn từ thù địch lặp lại",
            "fraud", "Hành vi gian lận nạp Coin / trục lợi hệ thống",
            "other", "Lý do khác"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getUsers(String keyword, String role, String status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 100) ? 20 : size;

        String safeKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Integer keywordId = null;
        if (safeKeyword != null) {
            String numericCandidate = safeKeyword;
            if (numericCandidate.toUpperCase().startsWith("USR-") || numericCandidate.toUpperCase().startsWith("#USR-")) {
                numericCandidate = numericCandidate.replaceAll("(?i)#?USR-", "");
            } else if (numericCandidate.startsWith("#")) {
                numericCandidate = numericCandidate.substring(1);
            }
            if (numericCandidate.matches("^\\d+$")) {
                try {
                    keywordId = Integer.parseInt(numericCandidate);
                } catch (NumberFormatException ignored) {}
            }
        }

        String safeRole = (role != null && VALID_ROLES.contains(role.trim())) ? role.trim() : null;
        String safeStatus = (status != null && VALID_STATUSES.contains(status.trim())) ? status.trim() : null;

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.searchUsers(safeKeyword, keywordId, safeRole, safeStatus, pageable);

        return userPage.map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus("ACTIVE");
        long banned = userRepository.countByStatus("BANNED");

        String percentage = "0%";
        if (total > 0) {
            double pct = ((double) active / total) * 100.0;
            percentage = String.format(Locale.US, "%.1f%%", pct);
        }

        return UserStatsDTO.builder()
                .totalUsers(total)
                .activeUsers(active)
                .activePercentage(percentage)
                .bannedUsers(banned)
                .build();
    }

    @Override
    @Transactional
    public void assignRole(Integer adminId, AssignRoleRequest request, String ipAddress) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("Thông tin yêu cầu phân quyền không hợp lệ");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản không hợp lệ (chỉ áp dụng cho tài khoản đang hoạt động)");
        }

        boolean isTargetAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        if (isTargetAdmin) {
            throw new IllegalStateException("Không thể thay đổi vai trò của quản trị viên");
        }

        if (user.getId().equals(adminId)) {
            throw new IllegalStateException("Không thể tự thay đổi vai trò của chính mình");
        }

        boolean currentStaff = user.getRoles().stream().anyMatch(r -> "ROLE_STAFF".equals(r.getName()));
        boolean targetStaff = Boolean.TRUE.equals(request.getHasStaffRole());

        if (currentStaff == targetStaff) {
            throw new IllegalArgumentException("NO_CHANGE");
        }

        // Preserve and ensure default ROLE_MEMBER
        Role memberRole = roleRepository.findByName("ROLE_MEMBER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_MEMBER").build()));
        user.getRoles().add(memberRole);

        Set<String> oldRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        if (targetStaff) {
            Role staffRole = roleRepository.findByName("ROLE_STAFF")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STAFF").build()));
            user.getRoles().add(staffRole);
        } else {
            user.getRoles().removeIf(r -> "ROLE_STAFF".equals(r.getName()));
        }

        userRepository.save(user);
        Set<String> newRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        // 1. Audit Log
        try {
            String oldValJson = objectMapper.writeValueAsString(Map.of("roles", oldRoleNames));
            String newValJson = objectMapper.writeValueAsString(Map.of("roles", newRoleNames));

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId)
                    .actionType("CHANGE_USER_ROLE")
                    .targetEntity("USERS")
                    .targetId(user.getId())
                    .oldValue(oldValJson)
                    .newValue(newValJson)
                    .ipAddress(ipAddress)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log phân quyền", e);
        }

        // 2. Notification
        try {
            String notifMsg = targetStaff
                    ? "Tài khoản của bạn đã được bổ nhiệm vai trò Nhân viên điều phối (ROLE_STAFF)."
                    : "Vai trò Nhân viên điều phối trên tài khoản của bạn đã được thu hồi.";

            notificationRepository.save(Notification.builder()
                    .userId(user.getId())
                    .title("Thông báo thay đổi vai trò tài khoản")
                    .content(notifMsg)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo phân quyền", e);
        }
    }

    @Override
    @Transactional
    public void banUser(Integer adminId, BanUserRequest request, String ipAddress) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("Thông tin yêu cầu khóa không hợp lệ");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng"));

        if (user.getId().equals(adminId)) {
            throw new IllegalStateException("Bạn không thể khóa tài khoản của chính mình");
        }

        boolean isTargetAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        if (isTargetAdmin) {
            throw new IllegalStateException("Không thể khóa tài khoản quản trị viên");
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản này đã bị khóa từ trước");
        }

        if (!VALID_BAN_REASONS.contains(request.getReason())) {
            throw new IllegalArgumentException("Lý do xử lý không hợp lệ");
        }

        if (!VALID_BAN_DURATIONS.contains(request.getDuration())) {
            throw new IllegalArgumentException("Thời hạn đình chỉ không hợp lệ");
        }

        String note = request.getNote() != null ? request.getNote().trim() : "";
        if ("other".equals(request.getReason()) && note.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ghi chú nội bộ chi tiết khi chọn lý do khác");
        }
        if (note.length() > 500) {
            throw new IllegalArgumentException("Ghi chú nội bộ tối đa 500 ký tự");
        }

        LocalDateTime bannedAt = LocalDateTime.now();
        LocalDateTime expiresAt = null;
        if ("3".equals(request.getDuration())) {
            expiresAt = bannedAt.plusDays(3);
        } else if ("7".equals(request.getDuration())) {
            expiresAt = bannedAt.plusDays(7);
        } else if ("30".equals(request.getDuration())) {
            expiresAt = bannedAt.plusDays(30);
        }

        String oldStatus = user.getStatus();
        user.setStatus("BANNED");
        userRepository.save(user);

        // Audit Log stores ban details
        try {
            Map<String, Object> banDetails = new HashMap<>();
            banDetails.put("status", "BANNED");
            banDetails.put("reason", request.getReason());
            banDetails.put("reasonDisplay", BAN_REASON_MAP.getOrDefault(request.getReason(), request.getReason()));
            banDetails.put("duration", request.getDuration());
            banDetails.put("note", note);
            banDetails.put("bannedAt", bannedAt.toString());
            banDetails.put("banExpiresAt", expiresAt != null ? expiresAt.toString() : null);

            String oldValJson = objectMapper.writeValueAsString(Map.of("status", oldStatus));
            String newValJson = objectMapper.writeValueAsString(banDetails);

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId)
                    .actionType("BAN_USER")
                    .targetEntity("USERS")
                    .targetId(user.getId())
                    .oldValue(oldValJson)
                    .newValue(newValJson)
                    .ipAddress(ipAddress)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log khóa tài khoản", e);
        }
    }

    @Override
    @Transactional
    public void unbanUser(Integer adminId, UnbanUserRequest request, String ipAddress) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("Thông tin yêu cầu mở khóa không hợp lệ");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!"BANNED".equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản không ở trạng thái bị khóa");
        }

        String unbanReason = request.getUnbanReason() != null ? request.getUnbanReason().trim() : "";
        if (unbanReason.length() < 20 || unbanReason.length() > 500) {
            throw new IllegalArgumentException("Ghi chú thẩm định & Lý do mở khóa phải từ 20 đến 500 ký tự");
        }

        user.setStatus("ACTIVE");
        userRepository.save(user);

        // Audit log
        try {
            Map<String, Object> unbanDetails = new HashMap<>();
            unbanDetails.put("status", "ACTIVE");
            unbanDetails.put("unbanReason", unbanReason);
            unbanDetails.put("unbannedAt", LocalDateTime.now().toString());

            String oldValJson = objectMapper.writeValueAsString(Map.of("status", "BANNED"));
            String newValJson = objectMapper.writeValueAsString(unbanDetails);

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId)
                    .actionType("UNBAN_USER")
                    .targetEntity("USERS")
                    .targetId(user.getId())
                    .oldValue(oldValJson)
                    .newValue(newValJson)
                    .ipAddress(ipAddress)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi audit log mở khóa tài khoản", e);
        }

        // Notification
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            try {
                notificationRepository.save(Notification.builder()
                        .userId(user.getId())
                        .title("Tài khoản của bạn đã được mở khóa")
                        .content("Tài khoản của bạn đã được quản trị viên khôi phục quyền truy cập trên hệ thống Trạm Truyện.")
                        .build());
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo mở khóa", e);
            }
        }
    }

    private UserManagementDTO toDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        boolean isAdmin = roleNames.contains("ROLE_ADMIN");
        boolean isStaff = roleNames.contains("ROLE_STAFF");
        boolean isBanned = "BANNED".equalsIgnoreCase(user.getStatus());

        String highestRole;
        String roleDisplayName;
        if (isAdmin) {
            highestRole = "ROLE_ADMIN";
            roleDisplayName = "Quản trị viên";
        } else if (isStaff) {
            highestRole = "ROLE_STAFF";
            roleDisplayName = "Nhân viên";
        } else {
            highestRole = "ROLE_MEMBER";
            roleDisplayName = "Độc giả";
        }

        String statusDisplay = "Đang hoạt động";
        if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            statusDisplay = "Chờ kích hoạt";
        } else if (isBanned) {
            statusDisplay = "Bị khóa";
        }

        String formattedDate = "";
        if (user.getCreatedAt() != null) {
            formattedDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Format coin balance with dot separator (1.250)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        String formattedBalance = df.format(user.getWalletBalance() != null ? user.getWalletBalance() : 0);

        String initial = getInitials(user.getFullName());

        // Ban info
        String lastBanReason = "";
        String lastBanReasonKey = "";
        String lastBanDate = "";
        String lastBanNote = "";

        if (isBanned) {
            Optional<SystemAuditLog> lastBanLog = systemAuditLogRepository
                    .findFirstByTargetEntityAndTargetIdAndActionTypeOrderByCreatedAtDesc("USERS", user.getId(), "BAN_USER");
            if (lastBanLog.isPresent()) {
                SystemAuditLog logEntry = lastBanLog.get();
                if (logEntry.getCreatedAt() != null) {
                    lastBanDate = logEntry.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                if (logEntry.getNewValue() != null && !logEntry.getNewValue().isBlank()) {
                    try {
                        Map<String, Object> data = objectMapper.readValue(logEntry.getNewValue(), new TypeReference<>() {});
                        lastBanReasonKey = String.valueOf(data.getOrDefault("reason", ""));
                        lastBanReason = String.valueOf(data.getOrDefault("reasonDisplay", ""));
                        if (lastBanReason.isBlank() && !lastBanReasonKey.isBlank()) {
                            lastBanReason = BAN_REASON_MAP.getOrDefault(lastBanReasonKey, lastBanReasonKey);
                        }
                        lastBanNote = String.valueOf(data.getOrDefault("note", ""));
                    } catch (Exception ignored) {}
                }
            }
            if (lastBanReason.isBlank()) {
                lastBanReason = "Vi phạm điều khoản cộng đồng";
            }
            if (lastBanDate.isBlank() && user.getUpdatedAt() != null) {
                lastBanDate = user.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        }

        return UserManagementDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .walletBalance(user.getWalletBalance() != null ? user.getWalletBalance() : 0)
                .formattedBalance(formattedBalance)
                .status(user.getStatus())
                .statusDisplayName(statusDisplay)
                .createdAt(user.getCreatedAt())
                .createdAtFormatted(formattedDate)
                .roles(roleNames)
                .highestRole(highestRole)
                .roleDisplayName(roleDisplayName)
                .banned(isBanned)
                .admin(isAdmin)
                .staff(isStaff)
                .initial(initial)
                .lastBanReason(lastBanReason)
                .lastBanReasonKey(lastBanReasonKey)
                .lastBanDate(lastBanDate)
                .lastBanNote(lastBanNote)
                .build();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            String p = parts[0];
            return p.length() >= 2 ? p.substring(0, 2).toUpperCase() : p.toUpperCase();
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }
}
