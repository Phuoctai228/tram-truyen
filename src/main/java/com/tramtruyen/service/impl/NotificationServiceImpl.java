package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.SendNotificationRequest;
import com.tramtruyen.dto.UserRecipientDTO;
import com.tramtruyen.entity.Notification;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NotificationRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final PlatformTransactionManager transactionManager;

    public static final String REPORT_NOTIFICATION_PREFIX = NotificationService.REPORT_NOTIFICATION_PREFIX;
    private static final Pattern CODE_PATTERN = Pattern.compile("#(RP|FB|TR|CR)-\\d+", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Integer userId, String tab, int page, int size) {
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
    @Transactional(readOnly = true)
    public long getUnreadGeneralCount(Integer userId) {
        if (userId == null) return 0;
        return notificationRepository.countUnreadGeneralByUserId(userId, REPORT_NOTIFICATION_PREFIX);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadReportCount(Integer userId) {
        if (userId == null) return 0;
        return notificationRepository.countUnreadReportByUserId(userId, REPORT_NOTIFICATION_PREFIX);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationDetail(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) return null;
        Notification n = notificationRepository.findById(notificationId).orElse(null);
        if (n == null) return null;
        if (n.getUserId() != null && !n.getUserId().equals(userId)) {
            return null; // Not accessible by this user
        }
        return mapToDTO(n);
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

        boolean isReport = NotificationService.isReportNotification(n.getTitle());

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

        String targetTab = isReport ? "reports" : "all";
        String statusBadge;
        String category;
        String categoryDisplayName;

        if (isReport) {
            category = "REPORT_FEEDBACK";
            categoryDisplayName = "Phản hồi báo cáo";
            statusBadge = (n.getTitle() != null && n.getTitle().contains("Từ chối")) ? "Từ chối" : "Đã xử lý";
        } else if (isSys) {
            category = "SYSTEM";
            categoryDisplayName = "Thông báo hệ thống";
            statusBadge = "Hệ thống";
        } else {
            category = "PERSONAL";
            categoryDisplayName = "Hộp thư cá nhân";
            statusBadge = "Cá nhân";

            if (n.getTitle() != null && (n.getTitle().toLowerCase().contains("thưởng") || n.getTitle().toLowerCase().contains("coin"))) {
                category = "REWARD";
                categoryDisplayName = "Thưởng & Tri ân";
                statusBadge = "Đã cộng Coin";
            } else if (n.getTitle() != null && n.getTitle().toLowerCase().contains("cảnh báo")) {
                category = "WARNING";
                categoryDisplayName = "Cảnh báo vi phạm";
                statusBadge = "Cần lưu ý";
            } else if (n.getTitle() != null && n.getTitle().startsWith("Chương mới:")) {
                category = "CHAPTER_UPDATE";
                categoryDisplayName = "Cập nhật chương";
                statusBadge = "Chương mới";
            }
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
                .targetTab(targetTab)
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

    @Override
    public void notifyNewChapterPublished(Integer novelId, Integer chapterId) {
        if (novelId == null || chapterId == null) {
            return;
        }

        Runnable task = () -> executeChapterNotification(novelId, chapterId);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void executeChapterNotification(Integer novelId, Integer chapterId) {
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            transactionTemplate.executeWithoutResult(status -> {
                Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId).orElse(null);
                if (novel == null || "ARCHIVED".equalsIgnoreCase(novel.getStatus())) {
                    log.warn("Novel {} is not active or archived, skipping chapter notifications", novelId);
                    return;
                }

                Chapter chapter = chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(chapterId, novelId).orElse(null);
                if (chapter == null) {
                    log.warn("Chapter {} not found for novel {}, skipping notifications", chapterId, novelId);
                    return;
                }

                String chapterStatus = chapter.getStatus();
                if (!ChapterStatus.PUBLISHED_REGULAR.name().equalsIgnoreCase(chapterStatus)
                        && !ChapterStatus.PUBLISHED_VIP.name().equalsIgnoreCase(chapterStatus)) {
                    log.info("Chapter {} status is '{}', not published. Skipping notifications", chapterId, chapterStatus);
                    return;
                }

                String novelTitle = novel.getTitle() != null ? novel.getTitle().trim() : "Truyện";
                Integer chapterNumber = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;
                String chapterTitle = chapter.getTitle() != null ? chapter.getTitle().trim() : ("Chương " + chapterNumber);
                Integer price = chapter.getPrice() != null ? chapter.getPrice() : 0;
                Integer uploaderId = novel.getUploaderId();

                // Build title: "Chương mới: {Tên truyện} - Chương {Số chương}"
                String baseTitle = "Chương mới: " + novelTitle + " - Chương " + chapterNumber;
                String title = (baseTitle.length() > 255) ? (baseTitle.substring(0, 252) + "...") : baseTitle;

                // Build content without publication time:
                String typeStr = (price > 0) ? ("Chương VIP - " + price + " Coin") : "Chương thường (Miễn phí)";
                String content = "Tác phẩm \"" + novelTitle + "\" trong Tủ sách của bạn vừa cập nhật chương mới:\n"
                        + "• Chương " + chapterNumber + ": " + chapterTitle + "\n"
                        + "• Hình thức: " + typeStr + "\n\n"
                        + "Bạn có thể mở mục \"Tủ sách của tôi\" hoặc tìm kiếm truyện \"" + novelTitle + "\" tại Trạm Truyện để đón đọc ngay!";

                int insertedCount = notificationRepository.insertChapterNotificationsForFollowers(
                        novelId, chapterId, title, content, uploaderId);

                log.info("Successfully sent {} chapter notifications for novel '{}' chapter #{}",
                        insertedCount, novelTitle, chapterNumber);
            });
        } catch (Throwable t) {
            log.error("Failed to execute chapter notifications for novelId={}, chapterId={}: {}",
                    novelId, chapterId, t.getMessage(), t);
        }
    }
}
