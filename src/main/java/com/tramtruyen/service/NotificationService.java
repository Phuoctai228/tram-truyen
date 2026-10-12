package com.tramtruyen.service;

import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.SendNotificationRequest;
import com.tramtruyen.dto.UserRecipientDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    Page<NotificationDTO> getMailboxNotifications(Integer userId, String tab, int page, int size);

    long getUnreadCount(Integer userId);

    void markAllAsRead(Integer userId);

    void markAsRead(Integer notificationId, Integer userId);

    void deleteNotification(Integer notificationId, Integer userId);

    void sendNotification(Integer senderAdminId, SendNotificationRequest request, String clientIp);

    List<UserRecipientDTO> searchRecipients(String keyword);

    Page<NotificationDTO> getRecentNotifications(int page, int size);
}
