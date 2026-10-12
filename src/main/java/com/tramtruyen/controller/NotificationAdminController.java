package com.tramtruyen.controller;

import com.tramtruyen.dto.NotificationDTO;
import com.tramtruyen.dto.SendNotificationRequest;
import com.tramtruyen.dto.UserRecipientDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationAdminController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String showSendNotificationPage(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                           Model model) {
        Page<NotificationDTO> recentHistory = notificationService.getRecentNotifications(page, size);

        if (!model.containsAttribute("sendRequest")) {
            model.addAttribute("sendRequest", SendNotificationRequest.builder()
                    .scope("ALL")
                    .build());
        }

        model.addAttribute("history", recentHistory);
        model.addAttribute("currentPage", recentHistory.getNumber());
        model.addAttribute("totalPages", recentHistory.getTotalPages());
        model.addAttribute("totalElements", recentHistory.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageTitle", "Gửi thông báo hệ thống | Trạm Truyện Admin");

        return "admin/notifications/send";
    }

    @PostMapping("/send")
    public String sendNotification(@Valid @ModelAttribute("sendRequest") SendNotificationRequest request,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   HttpServletRequest httpRequest,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.sendRequest", bindingResult);
            redirectAttributes.addFlashAttribute("sendRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/notifications";
        }

        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            notificationService.sendNotification(adminId, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi thông báo thành công!");
        } catch (Exception e) {
            log.error("Error sending notification", e);
            redirectAttributes.addFlashAttribute("sendRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/notifications";
    }

    @GetMapping("/search-users")
    @ResponseBody
    public ResponseEntity<List<UserRecipientDTO>> searchRecipients(@RequestParam(value = "q", defaultValue = "") String keyword) {
        List<UserRecipientDTO> results = notificationService.searchRecipients(keyword);
        return ResponseEntity.ok(results);
    }

    private Integer getAdminId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        if (authentication != null) {
            return userRepository.findByEmail(authentication.getName())
                    .map(User::getId)
                    .orElse(1);
        }
        return 1;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
