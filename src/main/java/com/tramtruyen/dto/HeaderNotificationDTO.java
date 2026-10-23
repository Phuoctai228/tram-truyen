package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderNotificationDTO {

    @Builder.Default
    private long unreadCount = 0;

    @Builder.Default
    private List<NotificationDTO> recentNotifications = Collections.emptyList();

    public boolean hasUnread() {
        return unreadCount > 0;
    }
}
