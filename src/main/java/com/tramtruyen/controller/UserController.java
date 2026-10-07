package com.tramtruyen.controller;

import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller handling user profile overview and personal navigation (temporary prototype for verification).
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final BookshelfService bookshelfService;
    private final UserService userService;

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
    public String viewHistory() {
        // Redirect to bookshelf or profile for now
        return "redirect:/user/bookshelf";
    }
}
