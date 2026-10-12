package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.SendNotificationRequest;
import com.tramtruyen.dto.UserRecipientDTO;
import com.tramtruyen.entity.Notification;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.NotificationRepository;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final ObjectMapper objectMapper;

    public static final String REPORT_NOTIFICATION_PREFIX = "Phản hồi về báo cáo";
    private static final Pattern CODE_PATTERN = Pattern.compile("#(RP|FB|TR|CR)-\\d+", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMailboxNotifications(Integer userId, String tab, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Notification> entityPage;
        if ("reports".equalsIgnoreCase(tab)) {
            entityPage = notificationRepository.findReportNotifications(userId, REPORT_NOTIFICATION_PREFIX, pageable);
        } else {
            entityPage = notificationRepository.findGeneralNotifications(userId, REPORT_NOTIFICATION_PREFIX, pageable);
        }

        if (safePage >= entityPage.getTotalPages() && entityPage.getTotalPages() > 0) {
            int clampedPage = entityPage.getTotalPages() - 1;
            pageable = PageRequest.of(clampedPage, safeSize);
            if ("reports".equalsIgnoreCase(tab)) {
                entityPage = notificationRepository.findReportNotifications(userId, REPORT_NOTIFICATION_PREFIX, pageable);
            } else {
                entityPage = notificationRepository.findGeneralNotifications(userId, REPORT_NOTIFICATION_PREFIX, pageable);
            }
        }

        Set<Integer> userIds = entityPage.getContent().stream()
                .map(Notification::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<NotificationDTO> dtos = entityPage.getContent().stream()
                .map(n -> mapToDTO(n, userMap))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer userId) {
        if (userId == null) return 0;
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        if (userId == null) return;
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) return;
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Thông báo không còn tồn tại"));

        // If personal notification and owned by user
        if (n.getUserId() != null && n.getUserId().equals(userId)) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) return;
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Thông báo không còn tồn tại"));

        if (n.getUserId() == null) {
            throw new IllegalArgumentException("Thông báo toàn hệ thống không thể xóa");
        }

        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa thông báo này");
        }

        notificationRepository.delete(n);
    }

    @Override
    @Transactional
    public void sendNotification(Integer senderAdminId, SendNotificationRequest request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu gửi thông báo không hợp lệ");
        }

        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        String content = request.getContent() != null ? request.getContent().trim() : "";

        if (title.isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề thông báo không được để trống");
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Nội dung thông báo không được để trống");
        }

        Integer recipientUserId = null;
        if ("SPECIFIC".equalsIgnoreCase(request.getScope())) {
            if (request.getRecipientId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn người nhận hợp lệ");
            }
            User recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại trên hệ thống"));
            recipientUserId = recipient.getId();
        }

        Notification notification = Notification.builder()
                .userId(recipientUserId)
                .title(title)
                .content(content)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // System Audit Log
        try {
            Map<String, Object> logDetails = new HashMap<>();
            logDetails.put("scope", request.getScope());
            logDetails.put("recipientId", recipientUserId);
            logDetails.put("title", title);

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(senderAdminId != null ? senderAdminId : 1)
                    .actionType("SEND_NOTIFICATION")
                    .targetEntity("notifications")
                    .targetId(saved.getId())
                    .newValue(objectMapper.writeValueAsString(logDetails))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log gửi thông báo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRecipientDTO> searchRecipients(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }
        String clean = keyword.trim();
        List<User> users = userRepository.findTop10ByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(clean, clean);
        return users.stream().map(u -> UserRecipientDTO.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .initial(getInitials(u.getFullName()))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getRecentNotifications(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Notification> pageResult = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);

        if (safePage >= pageResult.getTotalPages() && pageResult.getTotalPages() > 0) {
            int clampedPage = pageResult.getTotalPages() - 1;
            pageable = PageRequest.of(clampedPage, safeSize);
            pageResult = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Set<Integer> userIds = pageResult.getContent().stream()
                .map(Notification::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<NotificationDTO> dtos = pageResult.getContent().stream()
                .map(n -> mapToDTO(n, userMap))
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    private NotificationDTO mapToDTO(Notification n) {
        return mapToDTO(n, Collections.emptyMap());
    }

    private NotificationDTO mapToDTO(Notification n, Map<Integer, User> userMap) {
        boolean isSys = (n.getUserId() == null);
        String formattedTime = formatTime(n.getCreatedAt());

        String refCode = null;
        if (n.getContent() != null) {
            Matcher m = CODE_PATTERN.matcher(n.getContent());
            if (m.find()) {
                refCode = m.group();
            }
        }
        if (refCode == null) {
            refCode = "#RP-" + (1000 + (n.getId() != null ? n.getId() : 0));
        }

        String statusBadge = isSys ? "Hệ thống" : "Đã xử lý";
        String category = isSys ? "SYSTEM" : "PERSONAL";
        String categoryDisplayName = isSys ? "Thông báo hệ thống" : "Hộp thư cá nhân";

        if (n.getTitle() != null && n.getTitle().toLowerCase().startsWith(REPORT_NOTIFICATION_PREFIX.toLowerCase())) {
            category = "REPORT_FEEDBACK";
            categoryDisplayName = "Phản hồi báo cáo";
            statusBadge = n.getTitle().contains("Từ chối") ? "Từ chối" : "Đã xử lý";
        } else if (n.getTitle() != null && (n.getTitle().toLowerCase().contains("thưởng") || n.getTitle().toLowerCase().contains("coin"))) {
            category = "REWARD";
            categoryDisplayName = "Thưởng & Tri ân";
            statusBadge = "Đã cộng Coin";
        } else if (n.getTitle() != null && n.getTitle().toLowerCase().contains("cảnh báo")) {
            category = "WARNING";
            categoryDisplayName = "Cảnh báo vi phạm";
            statusBadge = "Cần lưu ý";
        }

        String recipientName = "Toàn hệ thống";
        String recipientEmail = null;
        if (!isSys) {
            User u = userMap.get(n.getUserId());
            if (u != null) {
                recipientName = u.getFullName();
                recipientEmail = u.getEmail();
            } else {
                Optional<User> uOpt = userRepository.findById(n.getUserId());
                if (uOpt.isPresent()) {
                    recipientName = uOpt.get().getFullName();
                    recipientEmail = uOpt.get().getEmail();
                } else {
                    recipientName = "Người dùng #" + n.getUserId();
                }
            }
        }

        String sentTimeFormatted = n.getCreatedAt() != null
                ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "";

        return NotificationDTO.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .recipientName(recipientName)
                .recipientEmail(recipientEmail)
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(isSys ? false : Boolean.TRUE.equals(n.getIsRead()))
                .createdAt(n.getCreatedAt())
                .timeFormatted(formattedTime)
                .sentTimeFormatted(sentTimeFormatted)
                .category(category)
                .categoryDisplayName(categoryDisplayName)
                .referenceCode(refCode)
                .statusBadge(statusBadge)
                .isSystem(isSys)
                .build();
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(dt, now);
        if (diff.toMinutes() < 1) return "Vừa xong";
        if (diff.toMinutes() < 60) return diff.toMinutes() + " phút trước";
        if (diff.toHours() < 24 && dt.toLocalDate().isEqual(now.toLocalDate())) {
            return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (dt.toLocalDate().isEqual(now.toLocalDate().minusDays(1))) {
            return "Hôm qua";
        }
        if (diff.toDays() < 7) {
            return diff.toDays() + " ngày trước";
        }
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
