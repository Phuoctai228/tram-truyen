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

import java.util.Map;

@Controller
@RequestMapping("/user/mailbox")
@RequiredArgsConstructor
@Slf4j
public class MailboxController {

    private final NotificationService notificationService;
    private final BookshelfService bookshelfService;
    private final UserRepository userRepository;

    @GetMapping
    public String viewMailbox(Authentication authentication,
                              @RequestParam(value = "tab", defaultValue = "all") String tab,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId,
                              Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login?redirect=/user/mailbox";
        }

        Integer userId = getUserId(authentication);
        String email = authentication.getName();
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);

        Page<NotificationDTO> notificationsPage = notificationService.getMailboxNotifications(userId, tab, page, size);
        long unreadCount = notificationService.getUnreadCount(userId);

        NotificationDTO selectedNotification = null;
        if (selectedId != null) {
            for (NotificationDTO dto : notificationsPage.getContent()) {
                if (dto.getId().equals(selectedId)) {
                    selectedNotification = dto;
                    break;
                }
            }
        }
        if (selectedNotification == null && !notificationsPage.getContent().isEmpty()) {
            selectedNotification = notificationsPage.getContent().get(0);
        }

        // If selected notification is personal and unread, auto-mark as read
        if (selectedNotification != null && !selectedNotification.isSystem() && !Boolean.TRUE.equals(selectedNotification.getIsRead())) {
            notificationService.markAsRead(selectedNotification.getId(), userId);
            selectedNotification.setIsRead(true);
            unreadCount = Math.max(0, unreadCount - 1);
        }

        model.addAttribute("userProfile", userProfile);
        model.addAttribute("notifications", notificationsPage);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("activeTab", tab);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedNotification", selectedNotification);
        model.addAttribute("pageTitle", "Hòm thư độc giả - Trạm Truyện");

        return "user/mailbox";
    }

    @PostMapping("/mark-all-read")
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
        return "redirect:/user/mailbox?tab=" + tab + "&page=" + page;
    }

    @PostMapping("/{id}/read")
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

    @PostMapping("/{id}/delete")
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
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thông báo khỏi hòm thư.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/mailbox?tab=" + tab + "&page=" + page;
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
