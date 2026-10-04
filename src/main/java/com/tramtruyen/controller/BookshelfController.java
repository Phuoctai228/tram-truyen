package com.tramtruyen.controller;

import com.tramtruyen.dto.BookshelfPageDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tramtruyen.security.CustomAuthenticationSuccessHandler;

/**
 * Controller handling reader personal bookshelf web requests (M2-F06, M2-F07, M2-F08).
 */
@Controller
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @GetMapping("/tu-sach")
    public String tuSachAlias() {
        return "redirect:/user/bookshelf";
    }

    @GetMapping("/user/bookshelf")
    public String viewBookshelf(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "1") int page,
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/bookshelf";
        }

        String email = authentication.getName();
        BookshelfPageDTO bookshelfPage = bookshelfService.getBookshelfPage(email, filter, q, sort, page, 8);
        UserProfileDTO userProfile = bookshelfService.getUserProfile(email);

        model.addAttribute("bookshelfPage", bookshelfPage);
        model.addAttribute("userProfile", userProfile);
        return "user/bookshelf";
    }

    @PostMapping("/user/bookshelf/{novelId}")
    public String addNovelToBookshelf(
            @PathVariable Integer novelId,
            @RequestParam(required = false, defaultValue = "details") String source,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            String returnTarget = (source != null && source.startsWith("/")) ? source : ("/novel/" + novelId);
            return "redirect:/login?redirect=" + returnTarget;
        }

        String email = authentication.getName();
        try {
            bookshelfService.addNovelToBookshelf(email, novelId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thêm truyện vào tủ sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        if (source != null && source.startsWith("/") && CustomAuthenticationSuccessHandler.isValidRedirectUrl(source)) {
            return "redirect:" + source;
        }
        if ("bookshelf".equals(source)) {
            return "redirect:/user/bookshelf";
        }
        return "redirect:/novel/" + novelId;
    }

    @PostMapping("/user/bookshelf/{novelId}/remove")
    public String removeNovelFromBookshelf(
            @PathVariable Integer novelId,
            @RequestParam(required = false, defaultValue = "bookshelf") String source,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            String returnTarget = (source != null && source.startsWith("/")) ? source : ("/novel/" + novelId);
            return "redirect:/login?redirect=" + returnTarget;
        }

        String email = authentication.getName();
        try {
            bookshelfService.removeNovelFromBookshelf(email, novelId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa truyện khỏi tủ sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        if (source != null && source.startsWith("/") && CustomAuthenticationSuccessHandler.isValidRedirectUrl(source)) {
            return "redirect:" + source;
        }
        if ("details".equals(source)) {
            return "redirect:/novel/" + novelId;
        }
        return "redirect:/user/bookshelf";
    }

    @PostMapping("/user/bookshelf/{novelId}/mark-read")
    public String markNovelAsRead(
            @PathVariable Integer novelId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login?redirect=/user/bookshelf";
        }

        String email = authentication.getName();
        try {
            bookshelfService.markNovelAsRead(email, novelId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã đánh dấu đã đọc toàn bộ chương!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/user/bookshelf";
    }
}
