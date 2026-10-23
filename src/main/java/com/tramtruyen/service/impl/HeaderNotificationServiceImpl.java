package com.tramtruyen.service.impl;

import com.tramtruyen.dto.HeaderNotificationDTO;
import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.entity.Notification;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.NotificationRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.HeaderNotificationService;
import com.tramtruyen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("headerNotificationService")
@RequiredArgsConstructor
@Slf4j
public class HeaderNotificationServiceImpl implements HeaderNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final Pattern CODE_PATTERN = Pattern.compile("#(RP|FB|TR|CR)-\\d+", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional(readOnly = true)
    public HeaderNotificationDTO getHeaderData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return new HeaderNotificationDTO(0L, Collections.emptyList());
        }

        try {
            Integer userId = resolveUserId(authentication);
            if (userId == null) {
                return new HeaderNotificationDTO(0L, Collections.emptyList());
            }

            // Query 1: Unread count of personal notifications
            long unreadCount = notificationRepository.countUnreadByUserId(userId);

            // Query 2: Top 5 recent notifications (both personal and system)
            Pageable pageable = PageRequest.of(0, 5);
            Page<Notification> notifPage = notificationRepository.findUserAndSystemNotifications(userId, pageable);

            List<NotificationDTO> dtos = new ArrayList<>();
            if (notifPage != null && notifPage.hasContent()) {
                for (Notification n : notifPage.getContent()) {
                    boolean isSys = (n.getUserId() == null);
                    boolean isReport = NotificationService.isReportNotification(n.getTitle());
                    String targetTab = isReport ? "reports" : "all";

                    String statusBadge;
                    if (isReport) {
                        statusBadge = (n.getTitle() != null && n.getTitle().contains("Từ chối")) ? "Từ chối" : "Đã xử lý";
                    } else if (isSys) {
                        statusBadge = "Hệ thống";
                    } else {
                        statusBadge = "Cá nhân";
                        if (n.getTitle() != null && (n.getTitle().toLowerCase().contains("thưởng") || n.getTitle().toLowerCase().contains("coin"))) {
                            statusBadge = "Đã cộng Coin";
                        } else if (n.getTitle() != null && n.getTitle().toLowerCase().contains("cảnh báo")) {
                            statusBadge = "Cần lưu ý";
                        }
                    }

                    String refCode = null;
                    if (isReport) {
                        if (n.getTitle() != null) {
                            Matcher m = CODE_PATTERN.matcher(n.getTitle());
                            if (m.find()) {
                                refCode = m.group();
                            }
                        }
                        if (refCode == null && n.getContent() != null) {
                            Matcher m = CODE_PATTERN.matcher(n.getContent());
                            if (m.find()) {
                                refCode = m.group();
                            }
                        }
                    }

                    String timeFormatted = formatTime(n.getCreatedAt());

                    dtos.add(NotificationDTO.builder()
                            .id(n.getId())
                            .userId(n.getUserId())
                            .title(n.getTitle())
                            .content(n.getContent())
                            .isRead(isSys ? false : Boolean.TRUE.equals(n.getIsRead()))
                            .createdAt(n.getCreatedAt())
                            .timeFormatted(timeFormatted)
                            .isSystem(isSys)
                            .targetTab(targetTab)
                            .statusBadge(statusBadge)
                            .referenceCode(refCode)
                            .build());
                }
            }

            return new HeaderNotificationDTO(unreadCount, dtos);
        } catch (Exception e) {
            log.error("Lỗi khi tải thông báo header: {}", e.getMessage(), e);
            return new HeaderNotificationDTO(0L, Collections.emptyList());
        }
    }

    private Integer resolveUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails cud && cud.getUser() != null) {
            return cud.getUser().getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
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
}
