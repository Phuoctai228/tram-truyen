package com.tramtruyen.controller;

import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller handling user profile overview and personal navigation (temporary prototype for verification).
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final BookshelfService bookshelfService;

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

    @GetMapping("/history")
    public String viewHistory() {
        // Redirect to bookshelf or profile for now
        return "redirect:/user/bookshelf";
    }
}
