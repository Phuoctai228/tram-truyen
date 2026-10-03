package com.tramtruyen.controller;

import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @GetMapping
    public String viewBookshelf(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        List<Bookshelf> bookshelves = bookshelfService.getUserBookshelf(email);
        model.addAttribute("bookshelves", bookshelves);
        return "user/bookshelf";
    }

    @PostMapping("/{novelId}")
    public String addNovelToBookshelf(@PathVariable Integer novelId, 
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        try {
            bookshelfService.addNovelToBookshelf(email, novelId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thêm vào tủ sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/novel/" + novelId;
    }

    @PostMapping("/{novelId}/remove")
    public String removeNovelFromBookshelf(@PathVariable Integer novelId,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        try {
            bookshelfService.removeNovelFromBookshelf(email, novelId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa khỏi tủ sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/bookshelf";
    }
}
