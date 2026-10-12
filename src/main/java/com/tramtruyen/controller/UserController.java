package com.tramtruyen.controller;

import com.tramtruyen.dto.DailyCheckinResultDTO;
import com.tramtruyen.dto.DailyCheckinStatusDTO;
import com.tramtruyen.dto.ReadingHistoryItemDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.DailyCheckinService;
import com.tramtruyen.service.ReadingHistoryService;
import com.tramtruyen.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller handling user personal space (profile, reading history, daily check-in).
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final BookshelfService bookshelfService;
    private final UserService userService;
    private final ReadingHistoryService readingHistoryService;
    private final DailyCheckinService dailyCheckinService;

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/profile";
        }

        String email = authentication.getName();
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);
        int totalSaved = bookshelfService.getUserBookshelf(email).size();

        model.addAttribute("userProfile", userProfile);
        model.addAttribute("totalSavedNovels", totalSaved);
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String viewEditProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/edit-profile";
        }
        String email = authentication.getName();
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);
        model.addAttribute("userProfile", userProfile);
        return "user/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(Authentication authentication, 
                                @RequestParam("fullName") String fullName, 
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/edit-profile";
        }
        
        String email = authentication.getName();
        try {
            userService.updateProfile(email, fullName, avatar);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/edit-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật hồ sơ. Vui lòng thử lại sau.");
            return "redirect:/user/edit-profile";
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String viewChangePassword(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/change-password";
        }
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/change-password";
        }
        String email = authentication.getName();
        try {
            userService.changePassword(email, oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công");
            return "redirect:/user/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/change-password";
        }
    }

    @GetMapping("/history")
    public String viewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/history";
        }

        String email = authentication.getName();
        Page<ReadingHistoryItemDTO> historyPage = readingHistoryService.getReadingHistory(email, page, size);
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("historyItems", historyPage.getContent());
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("totalElements", historyPage.getTotalElements());

        return "user/history";
    }

    @PostMapping("/history/remove/{chapterId}")
    public String removeHistoryItem(
            @PathVariable Integer chapterId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        try {
            readingHistoryService.removeHistoryItem(authentication.getName(), chapterId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chương khỏi lịch sử đọc");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/history";
    }

    @PostMapping("/history/clear")
    public String clearHistory(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        try {
            readingHistoryService.clearHistory(authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ lịch sử đọc");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/history";
    }

    @GetMapping("/checkin")
    public String viewCheckin(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/checkin";
        }

        String email = authentication.getName();
        DailyCheckinStatusDTO checkinStatus = dailyCheckinService.getCheckinStatus(email);
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);

        model.addAttribute("status", checkinStatus);
        model.addAttribute("userProfile", userProfile);
        return "user/checkin";
    }

    @GetMapping("/api/checkin-status")
    @ResponseBody
    public ResponseEntity<DailyCheckinStatusDTO> getCheckinStatusApi(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(dailyCheckinService.getCheckinStatus(authentication.getName()));
    }

    @PostMapping("/checkin")
    public Object performCheckin(
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            if (isAjax) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để điểm danh"));
            }
            return "redirect:/login?redirect=/user/checkin";
        }

        try {
            DailyCheckinResultDTO result = dailyCheckinService.performCheckin(authentication.getName());
            if (isAjax) {
                return ResponseEntity.ok(result);
            }
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
            return "redirect:/user/checkin";
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/checkin";
        }
    }
}
