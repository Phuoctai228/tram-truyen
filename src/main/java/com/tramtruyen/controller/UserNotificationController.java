package com.tramtruyen.controller;

import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserNotificationController {

    private final NotificationService notificationService;
    private final BookshelfService bookshelfService;
    private final UserRepository userRepository;

    @GetMapping({"/user/notifications", "/user/notifications/"})
    public String viewNotifications(Authentication authentication,
                                    @RequestParam(value = "tab", defaultValue = "all") String tab,
                                    @RequestParam(value = "page", required = false) String page,
                                    @RequestParam(value = "size", required = false) String size,
                                    @RequestParam(value = "selectedId", required = false) String selectedIdParam,
                                    Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login?redirect=/user/notifications";
        }

        Integer userId = getUserId(authentication);
        String email = authentication.getName();
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);

        // Sanitize tab parameter
        String currentTab = "reports".equalsIgnoreCase(tab) ? "reports" : "all";

        // Parse selectedId safely without 500 on malformed string
        Integer selectedId = null;
        if (selectedIdParam != null && !selectedIdParam.isBlank()) {
            try {
                selectedId = Integer.parseInt(selectedIdParam.trim());
            } catch (NumberFormatException ignored) {
                // Ignore non-numeric selectedId without throwing 500
            }
        }

        NotificationDTO selectedNotification = null;
        if (selectedId != null) {
            // Check ownership & existence directly in DB (supports both personal and system)
            NotificationDTO detail = notificationService.getNotificationDetail(selectedId, userId);
            if (detail != null) {
                selectedNotification = detail;
                // If notification belongs to a different tab than currentTab, auto-switch to that tab
                String notifTab = detail.getTargetTab();
                if (!currentTab.equalsIgnoreCase(notifTab)) {
                    currentTab = notifTab;
                }
                // Auto mark as read if personal and unread
                if (!selectedNotification.isSystem() && !Boolean.TRUE.equals(selectedNotification.getIsRead())) {
                    notificationService.markAsRead(selectedNotification.getId(), userId);
                    selectedNotification.setIsRead(true);
                }
            }
        }

        int parsedPage = com.tramtruyen.util.PaginationUtils.parsePage(page);
        int parsedSize = com.tramtruyen.util.PaginationUtils.parseSize(size, 8);

        Page<NotificationDTO> notificationsPage = notificationService.getUserNotifications(userId, currentTab, parsedPage, parsedSize);

        if (parsedPage >= notificationsPage.getTotalPages() && notificationsPage.getTotalPages() > 0) {
            parsedPage = notificationsPage.getTotalPages() - 1;
            notificationsPage = notificationService.getUserNotifications(userId, currentTab, parsedPage, parsedSize);
        }

        // If no notification was selected (or selectedId wasn't found), fallback to first item on current page
        if (selectedNotification == null && !notificationsPage.getContent().isEmpty()) {
            selectedNotification = notificationsPage.getContent().get(0);
            if (!selectedNotification.isSystem() && !Boolean.TRUE.equals(selectedNotification.getIsRead())) {
                notificationService.markAsRead(selectedNotification.getId(), userId);
                selectedNotification.setIsRead(true);
            }
        }

        // Sync read status in current page list if selected item is present
        if (selectedNotification != null) {
            for (NotificationDTO dto : notificationsPage.getContent()) {
                if (dto.getId().equals(selectedNotification.getId())) {
                    dto.setIsRead(selectedNotification.getIsRead());
                    break;
                }
            }
        }

        // Fresh unread counts for all badges
        long unreadCount = notificationService.getUnreadCount(userId);
        long unreadGeneralCount = notificationService.getUnreadGeneralCount(userId);
        long unreadReportCount = notificationService.getUnreadReportCount(userId);

        String filterQuery = com.tramtruyen.util.PaginationUtils.buildFilterQuery(Map.of(
                "tab", currentTab
        ));

        model.addAttribute("userProfile", userProfile);
        model.addAttribute("notifications", notificationsPage);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("unreadGeneralCount", unreadGeneralCount);
        model.addAttribute("unreadReportCount", unreadReportCount);
        model.addAttribute("activeTab", currentTab);
        model.addAttribute("filterQuery", filterQuery);
        model.addAttribute("currentPage", notificationsPage.getNumber());
        model.addAttribute("totalPages", notificationsPage.getTotalPages());
        model.addAttribute("totalElements", notificationsPage.getTotalElements());
        model.addAttribute("pageSize", parsedSize);
        model.addAttribute("selectedNotification", selectedNotification);
        model.addAttribute("pageTitle", "Thông báo độc giả - Trạm Truyện");

        return "user/notifications";
    }

    /**
     * Backward-compatibility redirect from /user/mailbox to /user/notifications
     */
    @GetMapping({"/user/mailbox", "/user/mailbox/"})
    public String redirectOldMailbox(@RequestParam(value = "tab", required = false) String tab,
                                     @RequestParam(value = "page", required = false) String page,
                                     @RequestParam(value = "size", required = false) String size,
                                     @RequestParam(value = "selectedId", required = false) String selectedId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/user/notifications");
        if (tab != null && !tab.isBlank()) builder.queryParam("tab", tab);
        if (page != null && !page.isBlank()) builder.queryParam("page", page);
        if (size != null && !size.isBlank()) builder.queryParam("size", size);
        if (selectedId != null && !selectedId.isBlank()) builder.queryParam("selectedId", selectedId);
        return "redirect:" + builder.toUriString();
    }

    @PostMapping({"/user/notifications/mark-all-read", "/user/mailbox/mark-all-read"})
    public String markAllAsRead(Authentication authentication,
                                @RequestParam(value = "tab", defaultValue = "all") String tab,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        Integer userId = getUserId(authentication);
        notificationService.markAllAsRead(userId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu tất cả thông báo là đã đọc.");
        return "redirect:/user/notifications?tab=" + tab + "&page=" + page;
    }

    @PostMapping({"/user/notifications/{id}/read", "/user/mailbox/{id}/read"})
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable("id") Integer id, Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }
        Integer userId = getUserId(authentication);
        try {
            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping({"/user/notifications/{id}/delete", "/user/mailbox/{id}/delete"})
    public String deleteNotification(@PathVariable("id") Integer id,
                                     Authentication authentication,
                                     @RequestParam(value = "tab", defaultValue = "all") String tab,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        Integer userId = getUserId(authentication);
        try {
            notificationService.deleteNotification(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thông báo thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/notifications?tab=" + tab + "&page=" + page;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Integer getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
