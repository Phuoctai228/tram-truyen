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
        if ("home".equals(source)) {
            return "redirect:/";
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
        if ("home".equals(source)) {
            return "redirect:/";
        }
        if ("details".equals(source)) {
            return "redirect:/novel/" + novelId;
        }
        return "redirect:/user/bookshelf";
    }

    @PostMapping(value = "/api/user/bookshelf/{novelId}/toggle", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> toggleBookshelfAjax(
            @PathVariable Integer novelId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("success", false, "message", "Vui lòng đăng nhập để thao tác Tủ sách."));
        }
        String email = authentication.getName();
        try {
            boolean inBookshelf = bookshelfService.isNovelInBookshelf(email, novelId);
            if (inBookshelf) {
                bookshelfService.removeNovelFromBookshelf(email, novelId);
                return org.springframework.http.ResponseEntity.ok(
                        java.util.Map.of("success", true, "inBookshelf", false, "message", "Đã xóa truyện khỏi Tủ sách!"));
            } else {
                bookshelfService.addNovelToBookshelf(email, novelId);
                return org.springframework.http.ResponseEntity.ok(
                        java.util.Map.of("success", true, "inBookshelf", true, "message", "Đã thêm truyện vào Tủ sách!"));
            }
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/user/reading/complete-chapter", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> completeChapter(
            @RequestBody java.util.Map<String, Integer> payload,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("success", false, "message", "Vui lòng đăng nhập để lưu tiến độ."));
        }
        Integer chapterId = payload != null ? payload.get("chapterId") : null;
        if (chapterId == null) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", "Thiếu chapterId"));
        }
        String email = authentication.getName();
        try {
            bookshelfService.recordChapterRead(email, chapterId);
            return org.springframework.http.ResponseEntity.ok(
                    java.util.Map.of("success", true, "chapterId", chapterId, "message", "Đã lưu hoàn thành chương!"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message", "Lỗi khi lưu tiến độ: " + e.getMessage()));
        }
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
