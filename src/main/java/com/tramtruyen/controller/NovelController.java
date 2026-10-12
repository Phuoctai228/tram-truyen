package com.tramtruyen.controller;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.CategoryService;
import com.tramtruyen.service.ChapterService;
import com.tramtruyen.service.ChapterUnlockService;
import com.tramtruyen.service.NovelService;
import com.tramtruyen.service.RatingService;
import com.tramtruyen.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final BookshelfService bookshelfService;
    private final RatingService ratingService;
    private final CategoryService categoryService;
    private final ChapterUnlockService chapterUnlockService;
    private final WalletService walletService;

    @GetMapping("/search")
    public String searchNovels(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Integer> categories,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String chapterRange,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            Model model) {
            
        Integer minChapters = null;
        Integer maxChapters = null;
        if (chapterRange != null) {
            switch (chapterRange) {
                case "lt5": maxChapters = 4; break;
                case "5-10": minChapters = 5; maxChapters = 10; break;
                case "10-50": minChapters = 11; maxChapters = 50; break;
                case "gt50": minChapters = 51; break;
            }
        }

        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? 
                org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortParams[0]));

        org.springframework.data.domain.Page<Novel> results = novelService.advancedSearch(q, categories, status, minChapters, maxChapters, minRating, pageable);
        
        model.addAttribute("novelPage", results);
        model.addAttribute("novels", results.getContent());
        model.addAttribute("query", q);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        
        // Pass back current filters for UI state
        model.addAttribute("selectedCategories", categories != null ? categories : new java.util.ArrayList<>());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedChapterRange", chapterRange);
        model.addAttribute("selectedMinRating", minRating);
        model.addAttribute("currentSort", sort);

        return "home/search";
    }

    @GetMapping("/api/search/suggestions")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> searchSuggestions(@RequestParam(required = false) String q) {
        List<Novel> results = novelService.searchPublicNovels(q);
        // limit to 5
        if (results != null && results.size() > 5) {
            results = results.subList(0, 5);
        }
        
        List<java.util.Map<String, Object>> suggestions = new java.util.ArrayList<>();
        if (results != null) {
            for (Novel n : results) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("slug", n.getSlug());
                map.put("title", n.getTitle());
                map.put("author", n.getAuthor());
                map.put("coverUrl", n.getCoverUrl());
                // category logic (pick first)
                if (n.getCategories() != null && !n.getCategories().isEmpty()) {
                    map.put("category", n.getCategories().iterator().next().getName());
                } else {
                    map.put("category", "Khác");
                }
                suggestions.add(map);
            }
        }
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("results", suggestions);
        response.put("total", novelService.searchPublicNovels(q).size());
        return response;
    }

    @GetMapping("/truyen/{slug}")
    public String novelDetailsBySlug(
            @PathVariable String slug,
            org.springframework.security.core.Authentication authentication,
            Model model) {
        Novel novel = novelService.getPublicNovelBySlug(slug);
        List<Chapter> chapters = chapterService.getPublicChapters(novel.getId());
        boolean inBookshelf = false;
        com.tramtruyen.entity.NovelRating userRating = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
            userRating = ratingService.getUserRating(authentication.getName(), novel.getId());
        }

        long favoriteCount = bookshelfService.countNovelFavorites(novel.getId());
        List<Novel> sameAuthorNovels = novelService.getNovelsBySameAuthor(novel.getAuthor(), novel.getId());
        List<com.tramtruyen.entity.NovelRating> allRatings = ratingService.getRatingsByNovel(novel.getId());

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("sameAuthorNovels", sameAuthorNovels);
        model.addAttribute("userRating", userRating);
        model.addAttribute("allRatings", allRatings);
        return "novel/details";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/rating")
    public String submitRating(
            @PathVariable String slug,
            @RequestParam Integer rating,
            @RequestParam(required = false) String reviewText,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        try {
            ratingService.submitRating(authentication.getName(), novel.getId(), rating, reviewText);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho bộ truyện!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi gửi đánh giá. Vui lòng thử lại sau.");
        }
        return "redirect:/truyen/" + novel.getSlug();
    }

    @GetMapping("/novel/{id}")
    public String novelDetailsLegacy(@PathVariable Integer id) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug();
    }

    @GetMapping({"/truyen/{slug}/chapters", "/truyen/{slug}/danh-sach-chuong"})
    public String novelChaptersBySlug(@PathVariable String slug) {
        return "redirect:/truyen/" + slug + "#chapters";
    }

    @GetMapping("/novel/{id}/chapters")
    public String novelChaptersLegacy(@PathVariable Integer id) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug() + "#chapters";
    }

    @GetMapping({"/truyen/{slug}/chuong-{chapterNumber}", "/truyen/{slug}/read/{chapterNumber}"})
    public String readChapterBySlug(
            @PathVariable String slug,
            @PathVariable Integer chapterNumber,
            Authentication authentication,
            Model model) {
        Novel novel = novelService.getPublicNovelBySlug(slug);
        Chapter chapter = chapterService.getPublicChapter(novel.getId(), chapterNumber);

        boolean isLoggedIn = (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken));

        boolean isVip = chapter.getPrice() != null && chapter.getPrice() > 0;
        boolean isChapterUnlocked = !isVip;
        int walletBalance = 0;
        int remainingBalance = 0;
        boolean canAfford = false;

        if (isLoggedIn) {
            walletBalance = walletService.getBalance(authentication.getName());
            if (isVip) {
                isChapterUnlocked = chapterUnlockService.isChapterUnlocked(authentication.getName(), chapter.getId());
                remainingBalance = walletBalance - chapter.getPrice();
                canAfford = walletBalance >= chapter.getPrice();
            }
        }

        // Automatic reading activity tracking if unlocked
        boolean isChapterRead = false;
        boolean inBookshelf = false;
        if (isLoggedIn) {
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
            if (isChapterUnlocked) {
                bookshelfService.recordChapterRead(authentication.getName(), chapter.getId());
                isChapterRead = true;
            } else {
                isChapterRead = bookshelfService.isChapterRead(authentication.getName(), chapter.getId());
            }
        }

        // Server-side content security: If locked, extract first 2 paragraphs as preview only
        List<String> paragraphs;
        if (isChapterUnlocked) {
            paragraphs = Arrays.stream(chapter.getContent().replace("\r", "").split("\n"))
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .toList();
        } else {
            // Masked preview
            paragraphs = Arrays.stream(chapter.getContent().replace("\r", "").split("\n"))
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .limit(2)
                    .toList();
        }

        List<Chapter> chapters = chapterService.getPublicChapters(novel.getId());
        Integer prevChapterNumber = null;
        Integer nextChapterNumber = null;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getChapterNumber().equals(chapterNumber)) {
                if (i > 0) {
                    prevChapterNumber = chapters.get(i - 1).getChapterNumber();
                }
                if (i < chapters.size() - 1) {
                    nextChapterNumber = chapters.get(i + 1).getChapterNumber();
                }
                break;
            }
        }
        boolean isLatestChapter = (nextChapterNumber == null);

        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        model.addAttribute("chapters", chapters);
        model.addAttribute("prevChapterNumber", prevChapterNumber);
        model.addAttribute("nextChapterNumber", nextChapterNumber);
        model.addAttribute("isLatestChapter", isLatestChapter);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("isChapterRead", isChapterRead);

        // VIP Unlock attributes for UI Paywall and Modal (image10.png)
        model.addAttribute("isVip", isVip);
        model.addAttribute("isChapterUnlocked", isChapterUnlocked);
        model.addAttribute("paragraphs", paragraphs);
        model.addAttribute("userWalletBalance", walletBalance);
        model.addAttribute("remainingBalanceAfter", remainingBalance);
        model.addAttribute("canAfford", canAfford);
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "novel/reading";
    }

    @PostMapping({"/truyen/{slug}/chuong-{chapterNumber}/unlock", "/api/chapters/{chapterId}/unlock"})
    public Object unlockChapter(
            @PathVariable(required = false) String slug,
            @PathVariable(required = false) Integer chapterNumber,
            @PathVariable(required = false) Integer chapterId,
            @RequestParam(defaultValue = "false") Boolean autoUnlockNext,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            if (isAjax) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để mở khóa chương"));
            }
            return "redirect:/login?redirect=" + (slug != null ? "/truyen/" + slug + "/chuong-" + chapterNumber : "/");
        }

        Integer targetChapterId = chapterId;
        if (targetChapterId == null && slug != null && chapterNumber != null) {
            Novel novel = novelService.getPublicNovelBySlug(slug);
            Chapter chapter = chapterService.getPublicChapter(novel.getId(), chapterNumber);
            targetChapterId = chapter.getId();
        }

        try {
            UnlockResultDTO result = chapterUnlockService.unlockChapter(authentication.getName(), targetChapterId, autoUnlockNext);

            if (isAjax) {
                return ResponseEntity.ok(result);
            }

            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
            return "redirect:/truyen/" + slug + "/chuong-" + chapterNumber;
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/truyen/" + slug + "/chuong-" + chapterNumber;
        }
    }

    @GetMapping("/novel/{id}/read/{chapterNumber}")
    public String readChapterLegacy(
            @PathVariable Integer id,
            @PathVariable Integer chapterNumber) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug() + "/chuong-" + chapterNumber;
    }
}
