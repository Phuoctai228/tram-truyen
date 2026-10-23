package com.tramtruyen.service;

import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.SendNotificationRequest;
import com.tramtruyen.dto.UserRecipientDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    String REPORT_NOTIFICATION_PREFIX = "Phản hồi về báo cáo";

    static boolean isReportNotification(String title) {
        if (title == null) {
            return false;
        }
        return title.trim().toLowerCase().startsWith(REPORT_NOTIFICATION_PREFIX.toLowerCase());
    }

    Page<NotificationDTO> getUserNotifications(Integer userId, String tab, int page, int size);

    default Page<NotificationDTO> getMailboxNotifications(Integer userId, String tab, int page, int size) {
        return getUserNotifications(userId, tab, page, size);
    }

    long getUnreadCount(Integer userId);

    long getUnreadGeneralCount(Integer userId);

    long getUnreadReportCount(Integer userId);

    NotificationDTO getNotificationDetail(Integer notificationId, Integer userId);

    void markAllAsRead(Integer userId);

    void markAsRead(Integer notificationId, Integer userId);

    void deleteNotification(Integer notificationId, Integer userId);

    void sendNotification(Integer senderAdminId, SendNotificationRequest request, String clientIp);

    List<UserRecipientDTO> searchRecipients(String keyword);

    Page<NotificationDTO> getRecentNotifications(int page, int size);

    void notifyNewChapterPublished(Integer novelId, Integer chapterId);
}
